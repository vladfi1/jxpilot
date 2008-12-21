package net.sf.jxpilot.net;

import static net.sf.jxpilot.JXPilot.PRINT_PACKETS;
import static net.sf.jxpilot.net.Ack.putAck;
import static net.sf.jxpilot.net.Packet.*;
import static net.sf.jxpilot.net.ReplyData.readReplyData;
import static net.sf.jxpilot.util.Utilities.removeNullCharacter;

import java.io.*;
import java.net.*;
import java.nio.channels.DatagramChannel;

import javax.swing.JOptionPane;

import net.sf.jxpilot.AbstractClient;
import net.sf.jxpilot.data.Items;
import net.sf.jxpilot.data.Keys;
import net.sf.jxpilot.game.*;
import net.sf.jxpilot.map.BlockMap;
import net.sf.jxpilot.map.BlockMapSetup;
import net.sf.jxpilot.util.BitVector;
import net.sf.jxpilot.util.Utilities;
import net.sf.jgamelibrary.preferences.Preferences;

public class NetClient
{
	//private final Random rnd = new Random();
	
	/**
	 * MAGIC = 0x4401F4ED
	 * 
	 * 4401 is version number. This is the version used by fxi.
	 * Block maps and integer scores are used.
	 * 
	 * F4ED is the magic password-all xpilot clients/servers must have these as the lower 2 bytes of their magic.
	 */
	public static final int MAGIC =0x4401F4ED;
	
	//these are used for testing, should be changed later to support preferences
	public static final int TEAM = 0x0000FFFF;
	public static final String DISPLAY = "";
	public static final short POWER = 55;
	public static final short TURN_SPEED = 8;
	public static final short TURN_RESISTANCE = 0;
	public static final byte MAX_FPS = (byte)50;
	public static final byte[] MOTD_BYTES = {0,0,0x47,2,0,0x43,3};

	//used for sending display
	public static final short WIDTH_WANTED = 0x400;
	public static final short HEIGHT_WANTED = 0x400;
	public static final byte NUM_SPARK_COLORS = 0x08;
	public static final byte SPARK_RAND = 0x33;

	private final ShipShape SHIP = ShipShape.defaultShip;

	// Nick, user and host to send to server. This default values are for if
    // JXPilot was lauched w/o XPilotPanel.
    private String NICK = null;
    private String REAL_NAME = null;
    private String HOST = null;

    /**
     * Amount of time to wait for server before giving up.
     */
    public static final int SOCKET_TIMEOUT = 15*1000;
    
    /**
	 * 2^12=4096
	 */
	public static final int MAX_PACKET_SIZE = 4096;
	
	/**
	 * Largest possible map size = 2^16.
	 */
	public static final int MAX_MAP_SIZE = 65536;
	
	private InetSocketAddress server_address;
	private DatagramSocket socket;
	private DatagramChannel channel;
	private ByteBufferWrap out = new ByteBufferWrap(MAX_PACKET_SIZE);
	private ByteBufferWrap in = new ByteBufferWrap(MAX_PACKET_SIZE);
	private ByteBufferWrap map = new ByteBufferWrap(MAX_MAP_SIZE);
	private ByteBufferWrap reliableBuf = new ByteBufferWrap(MAX_PACKET_SIZE);
	private BlockMapSetup setup = new BlockMapSetup();
	private final PacketProcessor[] readers = new PacketProcessor[256];
	private ReplyData reply = new ReplyData();
	private ReliableData reliable = new ReliableData();
	private ReplyMessage message = new ReplyMessage();
	private BitVector keyboard = new BitVector(Keys.NUM_KEYS);
	private int last_keyboard_change=0;
	
	private short turn_speed, new_speed;
	
	/**
	 * Keeps track last frame update number.
	 * This is also used for sending acknowledgments.
	 */
	private int last_loops;
	private volatile boolean quit = false;
	
	/**
	 * keeps track of how far the mouse pointer has moved since the last frame update.
	 */
	private volatile short pointer_move_amount = 0;
	
	public static final int TALK_RETRY = 2;
	/**
	 * Used for sending talks.
	 */
	private volatile int talk_pending=0,talk_sequence_num=0, talk_last_send;
	
	//private Queue<String> talkQueue = new ArrayDeque<String>();
	private String talkString;
	
	//for measurement
	private long numPackets = 0;
	private long numPacketsReceived = 0;
	private long numFrames = 0;
	private long startTime;
	
	private AbstractClient client;	
	
	public NetClient(AbstractClient client)
	{
		this.client = client;
		
		//sets functions to handle packets
		
		//unreliable types
		readers[PKT_EYES]		= getEyesProcessor();
		readers[PKT_TIME_LEFT]	= getTimeLeftProcessor();
		readers[PKT_AUDIO]		= getAudioProcessor();
		readers[PKT_START]		= getStartProcessor();
		readers[PKT_END]		= new EndProcessor();
		readers[PKT_SELF]		= new SelfProcessor();
		readers[PKT_DAMAGED]	= new DamagedProcessor();
		readers[PKT_CONNECTOR]	= new ConnectorProcessor();
		readers[PKT_LASER]		= new LaserProcessor();
		readers[PKT_REFUEL]		= new RefuelProcessor();
		readers[PKT_SHIP]		= new ShipProcessor();
		readers[PKT_ECM]		= new ECMProcessor();
		readers[PKT_TRANS]		= getTransProcessor();
		readers[PKT_PAUSED]		= new PausedProcessor();
		readers[PKT_ITEM]		= new ItemProcessor();
		readers[PKT_MINE]		= new MineProcessor();
		readers[PKT_BALL]		= new BallProcessor();
		readers[PKT_MISSILE]	= new MissileProcessor();
		readers[PKT_SHUTDOWN]	= getShutdownProcessor();
		readers[PKT_DESTRUCT]	= getDestructProcessor();
		readers[PKT_SELF_ITEMS]	= new SelfItemsProcessor();
		readers[PKT_FUEL]		= new FuelProcessor();
		readers[PKT_CANNON]		= new CannonProcessor();
		readers[PKT_TARGET]		= getTargetProcessor();
		readers[PKT_RADAR]		= getRadarProcessor();
		readers[PKT_FASTRADAR]	= new FastRadarProcessor();
		readers[PKT_RELIABLE]	= getReliableProcessor();
		readers[PKT_QUIT]		= getQuitProcessor();
		readers[PKT_MODIFIERS]  = new ModifiersProcessor();
		readers[PKT_FASTSHOT]	= new FastShotProcessor();
		readers[PKT_THRUSTTIME] = getThrustTimeProcessor();
		readers[PKT_SHIELDTIME] = getShieldTimeProcessor();
		readers[PKT_PHASINGTIME]= getPhasingTimeProcessor();
		readers[PKT_ROUNDDELAY] = getRoundDelayProcessor();
		readers[PKT_LOSEITEM]	= getLoseItemProcessor();
		readers[PKT_WRECKAGE]	= new WreckageProcessor();
		readers[PKT_ASTEROID]	= getAsteroidProcessor();
		readers[PKT_WORMHOLE]	= getWormholeProcessor();
		
		PacketProcessor debrisProcessor = getDebrisProcessor();	
		int pkt_debris = Utilities.getUnsignedByte(PKT_DEBRIS);
		for (int i = 0;i<DEBRIS_TYPES;i++) {
			readers[i+pkt_debris] = debrisProcessor;
		}
		
		//reliable types
		readers[PKT_MOTD]		= getMOTDProcessor();
		readers[PKT_MESSAGE]	= getMessageProcessor();
		readers[PKT_TEAM_SCORE] = getTeamScoreProcessor();
		readers[PKT_PLAYER]		= getPlayerProcessor();
		readers[PKT_SCORE]		= getScoreProcessor();
		readers[PKT_TIMING]		= new TimingProcessor();
		readers[PKT_LEAVE]		= new LeaveProcessor();
		readers[PKT_WAR]		= new WarProcessor();
		readers[PKT_SEEK]		= getSeekProcessor();
		readers[PKT_BASE]		= getBaseProcessor();
		readers[PKT_QUIT]		= new QuitProcessor();
		readers[PKT_STRING]		= getStringProcessor();
		readers[PKT_SCORE_OBJECT]=new ScoreObjectProcessor();
		readers[PKT_TALK_ACK]	= new TalkAckProcessor();
		readers[PKT_REPLY]		= getReplyProcessor();
	}

	public String getNick(){return NICK;}
	public String getRealName(){return REAL_NAME;}
	public String getHost(){return HOST;}
	
	public void runClient(String serverIP, int serverPort)
	{
		try {
			server_address = new InetSocketAddress(serverIP, serverPort);

			channel = DatagramChannel.open();
			socket = channel.socket();
			//socket.connect(server_address);
			socket.setSoTimeout(SOCKET_TIMEOUT);
			channel.connect(server_address);

			System.out.println("Socket local port: " + socket.getLocalPort());
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Processing preferences for this client.
		Preferences prefs = client.getPreferences();
		if (prefs != null) {
			NICK = prefs.get("XPilotName");
			REAL_NAME = prefs.get("XPilotUser");
			HOST = prefs.get("XPilotHost");
		}

		if (NICK == null || NICK.isEmpty())
			NICK = System.getProperty("user.name");
		if (REAL_NAME == null || REAL_NAME.isEmpty())
			REAL_NAME = NICK;
		if (HOST == null || HOST.isEmpty())
			HOST = "java.client";

		try {
			sendJoinRequest(out, REAL_NAME, socket.getLocalPort(), NICK, HOST, TEAM);

			getReplyMessage(in, message);

			//getReplyMessage(in, message);
			System.out.println(message);

			while(message.getPack()!=ENTER_GAME_pack) {
				getReplyMessage(in, message);
				System.out.println(message);
			}

			int server_port = message.getValue();
			System.out.println("New server port: "+server_port);

			try
			{
				server_address = new InetSocketAddress(serverIP, server_port);
				channel.disconnect();
				channel.connect(server_address);
			}
			catch(IOException e)
			{
				e.printStackTrace();
				System.exit(1);
			}

			System.out.println("Sending Verify");
			sendVerify(out, REAL_NAME, NICK);

			ReliableDataError result=null;
			while (result!=ReliableDataError.NO_ERROR) {
				result = getReliableData(reliable, in);
				//System.out.println(result);

				if (result != ReliableDataError.BAD_PACKET && result != ReliableDataError.NOT_RELIABLE_DATA) {
					sendPacket(out);
				}
			}

			netSetup(in, map, setup, reliable);

			//map.flip();
			System.out.println(map.remaining()+"\n\nMap:\n");

			if(PRINT_PACKETS) setup.printMapData();

			client.mapInit(new BlockMap(setup));

			System.out.println("\nSending Net Start");
			netStart(out);

			//keyboard.clearBits();
			//keyboard.setBit(Keys.KEY_TURN_RIGHT, true);

			System.out.println("\nStarting input loop.");
			startTime = System.currentTimeMillis();

			inputLoop();

			System.out.println("\nEnd of input loop");
		}
		catch (InterruptedIOException e)
		{
			JOptionPane.showMessageDialog(null, "Server timed out!");
			client.handleTimeout();
		}
	}
	
	/**
	 * Main game method. Interprets server input and sends messages to client.
	 * The pointer, keyboard, and talk info are then sent to the server.
	 * @throws InterruptedIOException If the server takes too long.
	 */
	private void inputLoop() throws InterruptedIOException
	{	
		try {
			channel.configureBlocking(true);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		//long lastFrameTime = 0;
		//long min_interval = 1000000000/MAX_FPS;
		//long currentFrameTime;
		
		while(!quit) {
			
			//readPacket(in);
			readLatestPacket(in);
			
			netPacket(in, reliableBuf);
			
			client.handlePacketEnd();
			
			putPointerMove(out);
			putKeyboard(out, keyboard);
			sendTalk(out);
			sendPacket(out);
			out.clear();
		}
		
		sendQuit();
	}
	
	/**
	 * Temporary buffer used to store packets read from network.
	 */
	private ByteBufferWrap temp = new ByteBufferWrap(MAX_PACKET_SIZE);
	/**
	 * Reads the last packet sent into in.
	 * @param in The ByteBufferWrap in which the packet is read.
	 */
	private void readLatestPacket(ByteBufferWrap in) throws InterruptedIOException {
		try {
			channel.configureBlocking(true);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		readPacket(in);
		
		try {
			channel.configureBlocking(false);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		do {
			temp.clear();
			temp.putBytes(in);
		} while(readPacket(in)>0);
		
		in.clear();
		in.putBytes(temp);
	}
	
	public void putJoinRequest(ByteBufferWrap buf, String real_name, int port, String nick, String host, int team)
	{
		buf.clear();
		
		buf.putInt(MAGIC);
		buf.putString(real_name);
		//buf.putByte(PKT_MESSAGE);
		buf.putShort((short)port);
		
		buf.putByte(ENTER_QUEUE_pack);
		buf.putString(nick);
		
		buf.putString(DISPLAY);
		buf.putString(host);
		buf.putInt(team);
	}
	
	public void sendJoinRequest(ByteBufferWrap buf, String real_name, int port, String nick, String host, int team)
	{
		buf.clear();
		putJoinRequest(buf, real_name, port, nick, host, team);
		try
		{
			//buf.flip();
			buf.sendPacket(channel, server_address);	
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Note that this method clears buf.
	 * @param buf The buffer in which the input should be received.
	 * @return The number of bytes read, or -1 if no packets available.
	 */
	private int readPacket(ByteBufferWrap buf) throws InterruptedIOException
	{
		buf.clear();
		try
		{
			int read = buf.readPacket(channel);
			numPackets++;
			numPacketsReceived++;
			if (PRINT_PACKETS) System.out.println("\nGot Packet-number: " + numPackets + ", " + buf.position() + " bytes.");
			return read;
		} catch(InterruptedIOException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * Note that this method clears the buffer.
	 * @param buf The buffer from which the output should be sent.
	 */
	private void sendPacket(ByteBufferWrap buf) {
		if (buf.remaining() <= 0) return;
		
		try {
			buf.sendPacket(channel, server_address);
			numPackets++;
			buf.clear();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private static void putVerify(ByteBufferWrap buf, String real_name, String nick) {
		//buf.clear();
		buf.putByte(PKT_VERIFY);
		buf.putString(real_name);
		buf.putString(nick);
		buf.putString(DISPLAY);
	}
	
	private void sendVerify(ByteBufferWrap buf, String real_name, String nick) {
		buf.clear();
		putVerify(buf, real_name, nick);
		try {
			buf.sendPacket(channel, server_address);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void sendAck(ByteBufferWrap buf, Ack ack) {
		putAck(buf, ack);
		if(PRINT_PACKETS) System.out.println("\n"+ack);
	}
	
	public void sendAck(Ack ack) {
		sendAck(out, ack);
	}
	
	private static void putPower(ByteBufferWrap buf, short power) {
		buf.putByte(PKT_POWER);
		buf.putShort((short)(power*256));
	}
	private static void putPowerS(ByteBufferWrap buf, short power) {
		buf.putByte(PKT_POWER_S);
		buf.putShort((short)(power*256));
	}
	
	private static void putTurnSpeed(ByteBufferWrap buf, short turn_speed) {
		buf.putByte(PKT_TURNSPEED);
		buf.putShort((short)(turn_speed*256));
	}
	private static void putTurnSpeedS(ByteBufferWrap buf, short turn_speed) {
		buf.putByte(PKT_TURNSPEED_S);
		buf.putShort((short)(turn_speed*256));
	}
	private static void putTurnResistance(ByteBufferWrap buf, short turn_resistance) {
		buf.putByte(PKT_TURNRESISTANCE);
		buf.putShort((short)(turn_resistance*256));
	}
	private static void putTurnResistanceS(ByteBufferWrap buf, short turn_resistance) {
		buf.putByte(PKT_TURNRESISTANCE_S);
		buf.putShort((short)(turn_resistance*256));
	}
	private static void putDisplay(ByteBufferWrap buf) {
		buf.putByte(PKT_DISPLAY);
		buf.putShort(WIDTH_WANTED);
		buf.putShort(HEIGHT_WANTED);
		buf.putByte(NUM_SPARK_COLORS);
		buf.putByte(SPARK_RAND);
	}
	/**
	 * Puts a request for the MOTD.
	 * TODO: Make this method work correctly. Currently, it does not put in
	 * a correct MOTD request.
	 * @param buf The buffer to put the request into.
	 */
	private static void putMOTDRequest(ByteBufferWrap buf) {
		buf.putByte(PKT_MOTD);
		buf.putBytes(MOTD_BYTES);
	}
	
	/**
	 * 
	 * @param buf
	 * @param max_fps
	 */
	private static void putFPSRequest(ByteBufferWrap buf, byte max_fps) {
		buf.putByte(PKT_ASYNC_FPS);
		buf.putByte(max_fps);
	}
	
	/**
	 * Try to send a `start play' packet to the server and get an
	 * acknowledgement from the server.  This is called after
	 * we have initialized all our other stuff like the user interface
	 * and we also have the map already.
	 * 
	 * This method sends our PKT_SHAPE, ShipShape, PKT_PLAY, our power, turnspeed,
	 * turn resistance, display, and max fps request.
	 * 
	 */
	private void netStart(ByteBufferWrap out) {
		out.clear();
		
		out.putByte(PKT_SHAPE);
		out.putString(SHIP.toString());
		
		out.putByte(PKT_PLAY);
		putPower(out, POWER);
		putPowerS(out, POWER);
		putTurnSpeed(out, TURN_SPEED);
		putTurnSpeedS(out, TURN_SPEED);
		putTurnResistance(out, TURN_RESISTANCE);
		putTurnResistanceS(out, TURN_RESISTANCE);
		putDisplay(out);
		//putMOTDRequest(out);
		putFPSRequest(out, MAX_FPS);
		
		//out.flip();
		
		sendPacket(out);
	}
	
	
	private ReliableDataError getReliableData(ReliableData data, ByteBufferWrap in) throws InterruptedIOException {
		in.clear();
		readPacket(in);
		//in.flip();
		
		return data.readReliableData(in, this);
	}
	
	private ReplyMessage getReplyMessage(ByteBufferWrap in, ReplyMessage message) throws InterruptedIOException {
		readPacket(in);
		//buf.flip();
		return ReplyMessage.readReplyMessage(in, message);
	}
	
	private static int readMapPacket(ByteBufferWrap in, ByteBufferWrap map, ReliableData reliable) {
		//readReliableData(reliable, in);
		int remaining = in.remaining();
		System.out.println("Reliable len = "+reliable.getLen()+"\nMapPacket remaining = " + remaining);
		map.putBytes(in);
		return remaining;
	}
	
	private int getMapPacket(ByteBufferWrap in, ByteBufferWrap map, ReliableData reliable) throws InterruptedIOException {
		ReliableDataError error = getReliableData(reliable, in);
		
		if (error == ReliableDataError.NO_ERROR) {
			sendPacket(out);//sends the appropriate ack
			return readMapPacket(in, map, reliable);
		}
		
		return -1;
		
		//sendAck(out, Ack.ack.setAck(reliable));
		//System.out.println(reliable);
	}
	
	private static int readFirstMapPacket(ByteBufferWrap in, ByteBufferWrap map, BlockMapSetup setup, ReliableData reliable) {
		setup.readMapSetup(in);
		int remaining = in.remaining();
		System.out.println("Reliable len = "+reliable.getLen()+"\nFirstMapPacket remaining = " + remaining);
		map.putBytes(in);
		return remaining;
	}
	
	private int getFirstMapPacket(ByteBufferWrap in, ByteBufferWrap map, BlockMapSetup setup, ReliableData reliable) throws InterruptedIOException {
		ReliableDataError error = getReliableData(reliable, in);
		
		while(error!=ReliableDataError.NO_ERROR) {
			System.out.println("Didn't get reliable data when waiting for map\n" + error + "\n" + reliable);
			error = getReliableData(reliable, in);
		}
		
		sendPacket(out);
		
		return readFirstMapPacket(in, map, setup, reliable);		
	}
	
	private void putKeyboard(ByteBufferWrap buf, BitVector keyboard) {
		buf.putByte(PKT_KEYBOARD);
		buf.putInt(last_keyboard_change);
		
		synchronized(keyboard)
		{	
			buf.putBytes(keyboard.getBytes());
		}
		
		last_keyboard_change++;
	}
	
	public void movePointer(short amount) {
		pointer_move_amount += amount;
	}
	
	private void putPointerMove(ByteBufferWrap buf) {
		if(pointer_move_amount!=0) {
			buf.putByte(PKT_POINTER_MOVE);
			buf.putShort(pointer_move_amount);
			pointer_move_amount = 0;
		}
	}
	
	/**
	 * Receive the map data and some game parameters from
	 * the server.  The map data may be in compressed form.
	 * 
	 * Receives the first map packet, which has the setup, which
	 * tells us how much data the map has.
	 * 
	 * Then keeps receiving packets until done w/ data.
	 * 
	 * Uncompresses the map if necessary.
	 * 
	 */
	private void netSetup(ByteBufferWrap in, ByteBufferWrap map, BlockMapSetup setup, ReliableData reliable) throws InterruptedIOException {
		int i = getFirstMapPacket(in, map, setup, reliable);
		System.out.println(setup);
		
		//map.flip();
		
		int todo = setup.getMapDataLen()-i;
		
		//map.flip();
		
		while(todo>0) {
			i = getMapPacket(in, map, reliable);
			if (i>=0)
				todo -= i;
		}
		
		if (setup.getMapOrder() != BlockMapSetup.SETUP_MAP_UNCOMPRESSED) {
			//map.flip();
			setup.uncompressMap(map);
		}
	}
	
	/**
	 * Process a packet which most likely is a frame update,
	 * perhaps with some reliable data in it.
	 * 
	 * loops through in processing packets
	 * then loops through reliableBuf and processes as many packets as possible 
	 */
	private void netPacket(ByteBufferWrap in, ByteBufferWrap reliableBuf)
	{
		in.setReading();
		while(in.remaining()>0)
		{
			short type = in.peekUnsignedByte();
			
			if (readers[type]!=null)
			{
				try
				{
					readers[type].processPacket(in, client);
				}
				catch(PacketReadException e)
				{
					in.clear();
					e.printStackTrace();
				}
			}
			else
			{
				System.out.println("**********Unsupported type: " + type + "************");
				in.clear();
				break;
			}
		}

		reliableBuf.setReading();
		while (reliableBuf.remaining()>0)
		{
			if(PRINT_PACKETS)System.out.println("\nAttempting to read from reliableBuf");

			short type = reliableBuf.peekUnsignedByte();

			if(readers[type]!=null)
			{
				int pos = reliableBuf.position();//stores reliable buffer's position
				try
				{
					readers[type].processPacket(reliableBuf, client);
				}
				catch (ReliableReadException e) //happens if reliable data is broken up
				{
					if(PRINT_PACKETS)
						System.out.println("Fragmented reliable packet of type: " + type);
					reliableBuf.position(pos);
					break;
				}
				catch(PacketReadException e)//this should not happen
				{
					e.printStackTrace();
					break;
				}
			}
			else
			{
				System.out.println("**********Unsupported Reliable type: " + type + "************");
				break;
			}	
		}
		
		if (reliableBuf.remaining()<=0) {
			reliableBuf.clear();
		}
	}
	
	/**
	 * Processes XPilot packets.
	 * @author Vlad Firoiu
	 */
	protected interface PacketProcessor {
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException;	
		static final ReliableReadException reliableReadException = new ReliableReadException();
	}
	

	//public send methods for Client
	
	/**
	 * Number of quit packets to send to server.
	 */
	public static final int NUM_QUITS = 5;

	/**
	 * NetClient attempts to send a talk to the server.
	 * @param message Message to send to server.
	 */
	public void netTalk(String message)
	{
		talkString = message;
		talk_pending = ++talk_sequence_num;
		talk_last_send = last_loops - TALK_RETRY;
	}
	
	private void sendTalk(ByteBufferWrap out)
	{
		if (talk_pending == 0) {
			return;
		}
		
		if (last_loops - talk_last_send < TALK_RETRY) {
			return;
		}
		
		/*
		if (Packet_printf(&wbuf, "%c%ld%s", PKT_TALK,
				talk_pending, talk_str) == -1) {
			return -1;
		}
		*/
		
		out.putByte(PKT_TALK);
		out.putInt(talk_pending);
		//for(String s : talkQueue)
		out.putString(talkString);
		
		talk_last_send = last_loops;
	}

	/**
	 * Changes the turnspeed.
	 */
	public void netTurnSpeed(short new_speed) {
		this.new_speed = turn_speed;
	}
	
	private void sendTurnSpeed() {
		if(new_speed != turn_speed) {
			this.putTurnSpeed(out, new_speed);
			//this.putTurnSpeedS(out, turn_speed);
			new_speed = turn_speed;
		}
	}
	
	/**
	 * Stops input loop and sends quit packets.
	 */
	public void quit() {
		quit = true;
		//sendQuit();
	}
	
	private void sendQuit()
	{
		System.out.println("\nSending quit packets");
		synchronized(out)
		{
			for (int i = 0;i<NUM_QUITS;i++)
			{
				out.clear();
				out.putByte(PKT_QUIT);
				sendPacket(out);
			}
		}
		
		System.out.println('\n' + getStatistics());
		
	}
	
	public String getStatistics() {
		long runTime = (System.currentTimeMillis()-startTime)/1000;
		return "Client ran for " + runTime + " seconds.\n" +
		numPacketsReceived + " packets were received.\n" +
		numFrames + " frames were received.\n" +
		"Average fps = " + (double)numFrames/runTime;
	}
	
	//other methods client might need
	public BitVector getKeyboard() {
		synchronized(keyboard) {
			return keyboard;
		}
	}
	
	public void setKey(int key, boolean value) {
		synchronized(keyboard) {
			keyboard.setBit(key, value);
		}
	}
	
	
	//various classes to handle different packet types
	/**
	 * Processes reliable packets.
	 * @author Vlad Firoiu
	 */
	protected class ReliableProcessor implements PacketProcessor {
		public void processPacket(ByteBufferWrap in, AbstractClient client) {
			ReliableDataError error = reliable.readReliableData(in, NetClient.this, reliableBuf);
			if(PRINT_PACKETS) System.out.println(error);
		}
	}
	/**
	 * Note that subclasses should override this method if a separate reliable
	 * processor is to be used.
	 * @return A new {@code ReliableProcessor} object.
	 */
	protected PacketProcessor getReliableProcessor() {return new ReliableProcessor();}
	
	/**
	 * Processes reply packets.
	 * @author Vlad Firoiu
	 */
	protected class ReplyProcessor implements PacketProcessor {
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException {
			ReplyData data = readReplyData(in, reply);
			if(PRINT_PACKETS) System.out.println(data);
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate reply
	 * processor is to be used.
	 * @return A new {@code ReplyProcessor} object.
	 */
	protected PacketProcessor getReplyProcessor() {return new ReplyProcessor();}
	
	/**
	 * Processes quit packets.
	 * @author Vlad Firoiu
	 */
	protected class QuitProcessor implements PacketProcessor {
		protected QuitPacket quitPacket = new QuitPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException {
			quitPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n'+quitPacket.toString());
			client.handleQuit(quitPacket);
			System.out.println("Server closed connection: " + quitPacket.getReason());
		}
	}

	/**
	 * Note that subclasses should override this method if a separate quit
	 * processor is to be used.
	 * @return A new {@code QuitProcessor} object.
	 */
	protected PacketProcessor getQuitProcessor() {return new QuitProcessor();}
	
	/**
	 * Processes start packets.
	 * @author Vlad Firoiu
	 */
	protected class StartProcessor implements PacketProcessor {
		protected StartPacket startPacket = new StartPacket();
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException {
			startPacket.readPacket(in);

			//packet is duplicate or out of order
			if (last_loops >= startPacket.getLoops()) {
				in.clear();
				return;
			}

			numFrames++;
			last_loops = startPacket.getLoops();
			
			if(PRINT_PACKETS) System.out.println('\n' + startPacket.toString());
			
			client.handleStart(startPacket.getLoops());
		}
		
		/*
			int		n;
			long	loops;
			u_byte	ch;
			long	key_ack;

			if ((n = Packet_scanf(&rbuf,
					"%c%ld%ld",
					&ch, &loops, &key_ack)) <= 0) {
				return n;
			}
			if (last_loops >= loops) {

				// Packet is duplicate or out of order.

				Net_measurement(loops, PACKET_DROP);
				printf("ignoring frame (%ld)\n", last_loops - loops);
				return 0;
			}
			last_loops = loops;
			if (key_ack > last_keyboard_ack) {
				if (key_ack > last_keyboard_change) {
					printf("Premature keyboard ack by server (%ld,%ld,%ld)\n",
							last_keyboard_change, last_keyboard_ack, key_ack);

					 // Packet could be corrupt???
					 // Some blokes turn off checksumming.

					return 0;
				}
				else {
					last_keyboard_ack = key_ack;
				}
			}
			Net_lag_measurement(key_ack);
			if ((n = Handle_start(loops)) == -1) {
				return -1;
			}
			return 1;
		}
		 */
	}

	/**
	 * Note that subclasses should override this method if a separate start
	 * start is to be used.
	 * @return A new {@code ReliableProcessor} object.
	 */
	protected PacketProcessor getStartProcessor(){return new StartProcessor();}
	
	/**
	 * Processes Player packets.
	 * @author Vlad Firoiu
	 */
	protected class PlayerProcessor implements PacketProcessor {
		protected PlayerPacket playerPacket = new PlayerPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException {
			playerPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + playerPacket.toString());
			client.handlePlayer(playerPacket);
		}
	}

	/**
	 * Note that subclasses should override this method if a separate player
	 * processor is to be used.
	 * @return A new {@code PlayerProcessor} object.
	 */
	protected PacketProcessor getPlayerProcessor(){return new PlayerProcessor();}
	
	/**
	 * Processes score packets.
	 * @author Vlad Firoiu
	 */
	protected class ScoreProcessor implements PacketProcessor {
		protected ScorePacket scorePacket = new ScorePacket();

		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException {
			scorePacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + scorePacket.toString());
			client.handleScore(scorePacket.getId(), scorePacket.getScore(), scorePacket.getLife());
		}

		/*
			int Receive_score(void)
			{
				int			n;
				short		id, life;
				DFLOAT		score = 0;
				u_byte		ch, mychar, alliance = ' ';

				if (version < 0x4500) {
					short	rcv_score;
					n = Packet_scanf(&cbuf, "%c%hd%hd%hd%c", &ch,
							&id, &rcv_score, &life, &mychar);
					score = rcv_score;
					alliance = ' ';
				} else {
					// newer servers send scores with two decimals 
					int	rcv_score;
					n = Packet_scanf(&cbuf, "%c%hd%d%hd%c%c", &ch,
							&id, &rcv_score, &life, &mychar, &alliance);
					score = (DFLOAT)rcv_score / 100;
				}
				if (n <= 0) {
					return n;
				}
				if ((n = Handle_score(id, score, life, mychar, alliance)) == -1) {
					return -1;
				}
				return 1;
			}

		 */
	}

	/**
	 * Note that subclasses should override this method if a separate score
	 * processor is to be used.
	 * @return A new {@code ScoreProcessor} object.
	 */
	protected PacketProcessor getScoreProcessor(){return new ScoreProcessor();}
	
	/**
	 * Processes base packets.
	 * @author Vlad Firoiu
	 */
	protected class BaseProcessor implements PacketProcessor {
		protected BasePacket basePacket = new BasePacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException {
			basePacket.readPacket(in);
			if(PRINT_PACKETS)System.out.println('\n' + basePacket.toString());
			client.handleBase(basePacket);
		}
	}

	/**
	 * Note that subclasses should override this method if a separate base
	 * processor is to be used.
	 * @return A new {@code BaseProcessor} object.
	 */
	protected PacketProcessor getBaseProcessor(){return new BaseProcessor();}
	
	/**
	 * Processes message packets.
	 * @author Vlad Firoiu
	 */
	protected class MessageProcessor implements PacketProcessor {
		protected final MessagePacket messagePacket = new MessagePacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException {
			messagePacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + messagePacket.toString());
			client.handleMessage(messagePacket.getMessage());
		}
	}

	/**
	 * Note that subclasses should override this method if a separate message
	 * processor is to be used.
	 * @return A new {@code MessageProcessor} object.
	 */
	protected PacketProcessor getMessageProcessor(){return new MessageProcessor();}
	
	/**
	 * Processes debris packets.
	 * @author Vlad Firoiu
	 */
	protected class DebrisProcessor implements PacketProcessor {
		private DebrisPacket debrisPacket = new DebrisPacket();
		private AbstractDebrisHolder debrisHolder = new AbstractDebrisHolder();
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			debrisPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + debrisPacket.toString());
			for (int i = 0;i<debrisPacket.getNum();i++) {
				client.handleDebris(debrisHolder.setAbstractDebris(debrisPacket.getType()-DEBRIS_TYPES, in.getUnsignedByte(), in.getUnsignedByte()));
			}
		}
	}

	/**
	 * Note that subclasses should override this method if a separate debris
	 * processor is to be used.
	 * @return A new {@code DebrisProcessor} object.
	 */
	protected PacketProcessor getDebrisProcessor(){return new DebrisProcessor();}
	
	/*
		int Receive_debris(void)
		{
			int			n, r, type;

			if (rbuf.ptr - rbuf.buf + 2 >= rbuf.len) {
				return 0;
			}
			type = (*rbuf.ptr++ & 0xFF);
			n = (*rbuf.ptr++ & 0xFF);
			if (rbuf.ptr - rbuf.buf + (n * 2) > rbuf.len) {
				return 0;
			}
			r = Handle_debris(type - PKT_DEBRIS, (u_byte*)rbuf.ptr, n);
			rbuf.ptr += n * 2;

			return (r == -1) ? -1 : 1;
		}
	 */

	protected class SelfItemsProcessor implements PacketProcessor
	{
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			int mask = in.getInt();
			byte[] num_items = new byte[Items.NUM_ITEMS];

			for (int i = 0; mask != 0; i++) {
				if ((mask & (1 << i))!=0) {
					mask ^= (1 << i);
					if (i < Items.NUM_ITEMS) {
						num_items[i] = in.getByte();
					} else {
						in.getByte();
					}
				}
			}

			if(PRINT_PACKETS)
				System.out.println("\nSelf Items Packet\ntype = " + type +
					"\nmask = " + String.format("%x", mask));

		}
	}

	/*
		int Receive_self_items(void)
		{
			unsigned		mask;
			int			i, n;
			u_byte		ch;
			char		*rbuf_ptr_start = rbuf.ptr;
			u_byte		num_items[NUM_ITEMS];

			n = Packet_scanf(&rbuf, "%c%u", &ch, &mask);
			if (n <= 0) {
				return n;
			}
			memset(num_items, 0, sizeof num_items);
			for (i = 0; mask != 0; i++) {
				if (mask & (1 << i)) {
					mask ^= (1 << i);
					if (rbuf.ptr - rbuf.buf < rbuf.len) {
						if (i < NUM_ITEMS) {
							num_items[i] = *rbuf.ptr++;
						} else {
							rbuf.ptr++;
						}
					}
				}
			}

			Handle_self_items(num_items);
			return (rbuf.ptr - rbuf_ptr_start);
		}
	 */

	protected class SelfProcessor implements PacketProcessor
	{
		public static final int LENGTH = 
			(1 + 2 + 2 + 2 + 2 + 1) + (1 + 1 + 1 + 2 + 2 + 1 + 1 + 1) + (2 + 2 + 2 + 2 + 1 + 1) + 1;//31

		private final SelfHolder self = new SelfHolder();
		
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			if (in.remaining()<LENGTH)
			{
				System.out.println("\nPacket Self ("+in.remaining()+") is too small ("+ LENGTH+")!");
				in.clear();
				return;
			}

			byte type = in.getByte();
			short x = in.getShort();
			short y = in.getShort();
			short vx = in.getShort();
			short vy = in.getShort();
			byte heading = in.getByte();

			byte power = in.getByte();
			byte turnspeed = in.getByte();
			byte turnresistance = in.getByte();
			short lockId = in.getShort();
			short lockDist = in.getShort();
			byte lockDir = in.getByte();
			byte nextCheckPoint = in.getByte();

			byte currentTank = in.getByte();
			short fuelSum = in.getShort();
			short fuelMax = in.getShort();
			short ext_view_width = in.getShort();
			short ext_view_height = in.getShort();
			byte debris_colors = in.getByte();
			byte stat = in.getByte();
			byte autopilot_light = in.getByte();

			client.handleSelf(self.setSelf(x, y, vx, vy, 
					heading, power, turnspeed, turnresistance, 
					lockId, lockDist, lockDir, 
					nextCheckPoint, 
					currentTank, fuelSum, fuelMax, 
					ext_view_width, ext_view_height, 
					debris_colors, stat, autopilot_light));

			if(PRINT_PACKETS)
				System.out.println("\nPacket Self\ntype = " + type +
					"\nx = " + x +
					"\ny = " + y +
					"\nvx = " + vx +
					"\nvy = " + vy +
					"\nheading = " + heading +
					"\nautopilotLight = " + autopilot_light);
		}
	}

	protected class ModifiersProcessor implements PacketProcessor
	{
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{

			int pos = in.position();
			try
			{
				byte type = in.getByte();
				String mods = in.getString();

				if(PRINT_PACKETS)System.out.println("\nModifiers Packet\ntype = " + type +
						"\nmodifiers: " + mods);					
			}
			catch (StringReadException e)
			{
				e.printStackTrace();
				in.position(pos);
			}
		}
	}

	protected class EndProcessor implements PacketProcessor
	{
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			int loops = in.getInt();
			
			if(PRINT_PACKETS)
				System.out.println("\nEnd Packet\ntype = " + type +
					"\nloops = " + loops);
			
			client.handleEnd(loops);
		}
	}

	protected class BallProcessor implements PacketProcessor
	{
		private BallHolder b = new BallHolder();
		
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			short x = in.getShort();
			short y = in.getShort();
			short id = in.getShort();

			if(PRINT_PACKETS)
				System.out.println("\nBall Packet\ntype = " + type +
					"\nx = " + x +
					"\ny = " + y +
					"\nid = " + id);
			
			client.handleBall(b.setBall(x, y, id));
		}
	};

	protected class ShipProcessor implements PacketProcessor
	{
		public static final int LENGTH = 1 + 2 + 2 + 2 + 1 + 1;//9

		private ShipHolder ship = new ShipHolder();
		
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			short x = in.getShort();
			short y = in.getShort();
			short id = in.getShort();
			byte dir = in.getByte();
			byte flags = in.getByte();

			boolean shield = (flags & 1) != 0;
			boolean cloak = (flags & 2) != 0;
			boolean emergency_shield = (flags & 4) != 0;
			boolean phased = (flags & 8) != 0;
			boolean deflector = (flags & 0x10) != 0;

			if(PRINT_PACKETS)System.out.println("\nShip Packet\ntype = " + type +
					"\nx = " + x +
					"\ny = " + y +
					"\nid = " + id +
					"\ndir = " + dir +
					"\nshield: " + shield +
					"\ncloak: " + cloak +
					"\nemergency shield: " + emergency_shield +
					"\nphased: " + phased +
					"\ndeflector: " + deflector);

			client.handleShip(ship.setShip(x, y, id, dir, shield, cloak, emergency_shield, phased, deflector));
		}
	};

	protected class FastShotProcessor implements PacketProcessor
	{
		private AbstractDebrisHolder s = new AbstractDebrisHolder();
		
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException
		{
			byte pkt = in.getByte();
			byte type = in.getByte();
			short num = in.getUnsignedByte();

			if (in.remaining()<2*num)
			{
				in.clear();
				throw new PacketReadException();
			}

			//in.position(in.position()+2*num);

			if(PRINT_PACKETS)
				System.out.println("\nFastShot Packet\npkt = " + pkt +
									"\ntype = " + type +
									"\nnum = " + num);
			
			for(int i = 0;i<num;i++)
			{
				client.handleFastShot(s.setAbstractDebris(type, in.getUnsignedByte(), in.getUnsignedByte()));
			}
		}
	}

	protected class ItemProcessor implements PacketProcessor
	{
		public static final int LENGTH = 1 + 2 + 2 + 1;//6

		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte pkt = in.getByte();
			short x = in.getShort();
			short y = in.getShort();
			byte type = in.getByte();

			if(PRINT_PACKETS)
				System.out.println("\nItem Packet\npkt = " + pkt +
					"\nx = " + x +
					"\ny = " + y +
					"\ntype = " + type);

		}
	}

	protected class FastRadarProcessor implements PacketProcessor
	{
		private RadarHolder radar = new RadarHolder();
		
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			int n = (in.getByte() & 0xFF);
			int x, y, size;

			int pos = in.position();

			for (int i =0;i<n;i++)
			{
				x = in.getUnsignedByte();
				y= in.getUnsignedByte();

				byte b = in.getByte();

				y |= (b&0xC0) << 2;

				size = (b & 0x07);
				if ((b & 0x20)!=0)
				{
					size |= 0x80;
				}

				client.handleRadar(radar.setRadar(x, y, size));
			}

			if(PRINT_PACKETS)
				System.out.println("\nFast Radar Packet:\ntype = " + type +
					"\nn = " + n +
					"\nbuffer advanced " + (in.position()-pos));
		}

	}

	protected class PausedProcessor implements PacketProcessor
	{
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			short x = in.getShort();
			short y = in.getShort();
			short count = in.getShort();

			if(PRINT_PACKETS)System.out.println("\nPaused Packet:\ntype = " +type +
					"\nx = " + x +
					"\ny = " + y +
					"\ncount = " + count);
		}
	}

	protected class WreckageProcessor implements PacketProcessor
	{
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			short x= in.getShort();
			short y = in.getShort();
			byte wrecktype = in.getByte();
			byte size = in.getByte();
			byte rot = in.getByte();

			if(PRINT_PACKETS)
				System.out.println("\nWreckage Packet:\ntype = "+type +
					"\nx = " + x +
					"\ny = " + y +
					"\nwreck type = " + wrecktype +
					"\nsize = " + size +
					"\nrot = " + rot);

		}
	}

	protected class WarProcessor implements PacketProcessor
	{
		public static final int LENGTH = 1 + 2 + 2;//5

		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
		{

			if (in.remaining()<LENGTH) throw reliableReadException;

			byte type = in.getByte();
			short robot_id = in.getShort();
			short killer_id = in.getShort();

			if(PRINT_PACKETS)
				System.out.println("\nWar Packet\ntype = " + type +
					"\nRobot id = " + robot_id +
					"\nKiller id = " + killer_id);

		}
	};

	protected class ConnectorProcessor implements PacketProcessor
	{
		private ConnectorHolder c = new ConnectorHolder();
		
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			short x0 = in.getShort();
			short y0 = in.getShort();
			short x1 = in.getShort();
			short y1 = in.getShort();
			byte tractor = in.getByte();

			if(PRINT_PACKETS)
					System.out.println("\nConnector Packet\ntype = " + type +
					"\nx0 = " + x0 +
					"\ny0 = " + y0 +
					"\nx1 = " + x1 +
					"\ny1 = " + y1 +
					"\ntractor = " + tractor);
			
			client.handleConnector(c.setConnector(x0, y0, x1, y1, tractor));
		}
	}

	protected class LeaveProcessor implements PacketProcessor
	{
		public static final int LENGTH = 1 + 2;//3

		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
		{
			if (in.remaining()<LENGTH) throw reliableReadException;

			byte type = in.getByte();
			short id = in.getShort();

			if(PRINT_PACKETS)
				System.out.println("\nLeave Packet\ntype = " + type +
					"\nid = " + id);

			client.handleLeave(id);
		}
	};

	protected class ScoreObjectProcessor implements PacketProcessor
	{
		public static final int LENGTH = 1 + 2 + 2 + 2 + 1;//8
		
		private ScoreObjectHolder scoreObject = new ScoreObjectHolder();
		
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
		{
			if (in.remaining()<LENGTH) throw reliableReadException;

			try
			{
				byte type = in.getByte();
				//float score = (float)(in.getInt()/100.0);
				short score = in.getShort();
				int x  = in.getUnsignedShort();
				int y = in.getUnsignedShort();
				String message = in.getString();

				if(PRINT_PACKETS)
					System.out.println("\nScore Object Packet\ntype = " + type +
						"\nscore = " + score +
						"\nx = " + x +
						"\ny = " + y +
						"\nmessage: " + message);
				
				client.handleScoreObject(scoreObject.setScoreObject(score, x, y, message));
			}
			catch (StringReadException e)
			{
				throw reliableReadException;
			}
		}
		/*
			int Receive_score_object(void)
			{
				int			n;
				unsigned short	x, y;
				DFLOAT		score = 0;
				char		msg[MAX_CHARS];
				u_byte		ch;

				if (version < 0x4500) {
					short	rcv_score;
					n = Packet_scanf(&cbuf, "%c%hd%hu%hu%s",
							&ch, &rcv_score, &x, &y, msg);
					score = rcv_score;
				} else {
					// newer servers send scores with two decimals
					int	rcv_score;
					n = Packet_scanf(&cbuf, "%c%d%hu%hu%s",
							&ch, &rcv_score, &x, &y, msg);
					score = (DFLOAT)rcv_score / 100;
				}
				if (n <= 0)
					return n;
				if ((n = Handle_score_object(score, x, y, msg)) == -1) {
					return -1;
				}

				return 1;
			} 
		 */
	}

	protected class MineProcessor implements PacketProcessor
	{
		private MineHolder m = new MineHolder();
		
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			short x = in.getShort();
			short y = in.getShort();
			byte team_mine = in.getByte();
			short id = in.getShort();

			if(PRINT_PACKETS)System.out.println("\nMine Packet\ntype = " + type +
					"\nx = " + x +
					"\ny = " + y +
					"\nteam mine = " + team_mine +
					"\nid = " + id);
			
			client.handleMine(m.setMine(x, y, team_mine, id));
		}
	}

	protected class CannonProcessor implements PacketProcessor
	{
		private CannonHolder cannon = new CannonHolder();
		
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			int num = in.getUnsignedShort();
			int dead_time = in.getUnsignedShort();

			if(PRINT_PACKETS)
				System.out.println("\nCannon Packet\ntype = " + type + 
					"\nnum = " + num +
					"\ndead time = " + dead_time);
			
			
			
			out.putByte(PKT_ACK_CANNON);
			out.putInt(last_loops);
			out.putShort((short)num);
			
			client.handleCannon(cannon.set(num, dead_time));
		}
	}
	
	protected class FuelProcessor implements PacketProcessor
	{
		private FuelHolder f = new FuelHolder();
		
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			int num = in.getUnsignedShort();
			int fuel = in.getUnsignedShort();
			
			if(PRINT_PACKETS)
				System.out.println("\nFuel Packet\ntype = " + type +
									"\nnum = " + num +
									"\nfuel = " + fuel);
			
			
			out.putByte(PKT_ACK_FUEL);
			out.putInt(last_loops);
			out.putShort((short)num);
			
			client.handleFuel(f.setFuel(num, fuel));
		}
	}
	
	protected class MissileProcessor implements PacketProcessor
	{
		private MissileHolder m = new MissileHolder();
		
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			short x = in.getShort();
			short y = in.getShort();
			short len = in.getUnsignedByte();
			short dir = in.getUnsignedByte();
			
			if(PRINT_PACKETS)
			{
				System.out.println("\nMissile Packet\ntype = " + type +
									"\nx = " + x +
									"\ny = " + y +
									"\nlen = " + len +
									"\ndir = " + dir);
			}
			
			client.handleMissile(m.setMissile(x, y, len, dir));
		}
	}
	
	protected class DamagedProcessor implements PacketProcessor
	{
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			short damaged = in.getUnsignedByte();
			
			if (PRINT_PACKETS)
			{
				System.out.println("\nDamaged Packet\ntype = " + type +
									"\ndamaged = " + damaged);
			}
		}
	}
	
	protected class LaserProcessor implements PacketProcessor
	{
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			
			byte color = in.getByte();
			short x = in.getShort();
			short y = in.getShort();
			short len = in.getShort();
			short dir = in.getUnsignedByte();
			
			if(PRINT_PACKETS)
			{
				System.out.println("\nLaser Packet\ntype = " + type +
									"\ncolor = " + color +
									"\nx = " + x +
									"\ny = " + y +
									"\nlen = " + len +
									"\ndir = " + dir);
			}
		}
	}
	
	protected class ECMProcessor implements PacketProcessor
	{
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			short x = in.getShort();
			short y = in.getShort();
			short size = in.getShort();
			
			if (PRINT_PACKETS)
			{
				System.out.println("\nECM Packet\ntype = " + type +
									"\nx = " + x +
									"\ny = " + y +
									"\nsize = " + size);
			}
		}
	}
	
	protected class RefuelProcessor implements PacketProcessor
	{
		public void processPacket(ByteBufferWrap in, AbstractClient client)
		{
			byte type = in.getByte();
			short x0 = in.getShort();
			short y0 = in.getShort();
			short x1 = in.getShort();
			short y1 = in.getShort();
			
			if(PRINT_PACKETS)
			{
				System.out.println("\nRefuel Packet\ntype = " + type +
									"\nx0 = " + x0 +
									"\ny0 = " + y0 +
									"\nx1 = " + x1 +
									"\ny1 = " + y1);
			}
			
		}
	}
	
	protected class TalkAckProcessor implements PacketProcessor
	{
		public static final int LENGTH = 1+4;//5
		
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
		{
			if(in.remaining()<LENGTH) throw PacketProcessor.reliableReadException;
			
			byte type = in.getByte();
			int talk_ack = in.getInt();
			
			if(PRINT_PACKETS)
				System.out.println("\nTalk Ack Packet\ntype = " + type +
									"\ntalk ack = " + talk_ack);
			
			if (talk_ack >= talk_pending) {
				talk_pending = 0;
			}
		}
	}
	
	protected class TimingProcessor implements PacketProcessor
	{
		public static final int LENGTH = 1+2+2;//5
		
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
		{
			if(in.remaining()<LENGTH) throw PacketProcessor.reliableReadException;
			
			byte type = in.getByte();
			short id = in.getShort();
			int timing = in.getUnsignedShort();
			
			if(PRINT_PACKETS)
				System.out.println("\nTiming Packet\ntype = " + type +
									"\nid = " + id +
									"\ntiming = " + timing);
			
		}
	}
	
	/**
	 * Processes target packets.
	 * @author Vlad Firoiu
	 */
	protected class TargetProcessor implements PacketProcessor {
		protected TargetPacket targetPacket = new TargetPacket();
		
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			targetPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + targetPacket.toString());
			//acks target packet
			out.putByte(PKT_ACK_TARGET).putInt(last_loops).putShort(targetPacket.getNum());
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate target
	 * processor is to be used.
	 * @return A new TargetProcessor object.
	 */
	protected PacketProcessor getTargetProcessor(){return new TargetProcessor();}
	
	/**
	 * Processes eyes packets.
	 * @author Vlad Firoiu
	 */
	protected class EyesProcessor implements PacketProcessor {
		protected EyesPacket eyesPacket = new EyesPacket();
		
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			eyesPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + eyesPacket.toString());
			client.handleEyes(eyesPacket.getId());
		}
	}
	/**
	 * Note that subclasses should override this method if a separate eyes
	 * processor is to be used.
	 * @return A new EyesProcessor object.
	 */
	protected PacketProcessor getEyesProcessor(){return new EyesProcessor();}
	
	/**
	 * Processes Team Score packets.
	 * TODO: Implement handling of team score.
	 * @author Vlad Firoiu
	 */
	protected class TeamScoreProcessor implements PacketProcessor
	{
		protected byte pkt_type;
		protected short team;
		protected double score;
		
		public byte getPacketType() {return pkt_type;}
		public short getTeam() {return team;}
		public double getScore() {return score;}
		
		/**
		 * Reads the type, team, and score.
		 * @param in The buffer from which to read the data.
		 */
		protected void readPacket(ByteBufferWrap in) {
			pkt_type = in.getByte();
			team = in.getShort();
			score = (double)in.getInt()/100.0;
		}
		
		public void processPacket(ByteBufferWrap in, AbstractClient client) {
			readPacket(in);
			
			if(PRINT_PACKETS) System.out.println('\n' + this.toString());
		}
		
		@Override
		public String toString() {
			return "Eyes Packet\ntype = " + pkt_type +
					"\nteam = " + team +
					"\nscore = " + score;
		}
	}
	/**
	 * Note that subclasses should override this method if a separate team
	 * score processor is to be used.
	 * @return A new TeamScoreProcessor object.
	 */
	protected PacketProcessor getTeamScoreProcessor(){return new TeamScoreProcessor();}
	
	/**
	 * Processes Seek Packets.
	 * TODO: Implement handling of packet data.
	 * @author Vlad Firoiu
	 */
	protected class SeekProcessor implements PacketProcessor
	{
		protected byte pkt_type;
		protected short programmer_id, robot_id, sought_id;
		
		public byte getPacketType(){return pkt_type;}
		public short getProgrammerId(){return this.programmer_id;}
		public short getRobotId(){return this.robot_id;}
		public short getSoughtId(){return this.sought_id;}
		
		/**
		 * Reads the packet type, the programmer id, the robot id, and the
		 * sought id.
		 * @param in The buffer to read from.
		 */
		protected void readPacket(ByteBufferWrap in) {
			pkt_type = in.getByte();
			programmer_id = in.getShort();
			robot_id = in.getShort();
			sought_id = in.getShort();
		}
		
		public void processPacket(ByteBufferWrap in, AbstractClient client) {
			readPacket(in);
			
			if(PRINT_PACKETS) System.out.println('\n' + this.toString());
		}
		
		@Override
		public String toString() {
			return "Seek Packet\ntype = " + pkt_type +
					"\nprogrammer id = " + this.programmer_id +
					"\nrobot id = " + this.robot_id +
					"\nsought id = " + this.sought_id;
		}
	}
	/**
	 * Note that subclasses should override this method if a separate seek
	 * processor is to be used.
	 * @return A new SeekProcessor object.
	 */
	protected PacketProcessor getSeekProcessor(){return new SeekProcessor();}
	
	/**
	 * Processes String packets. Note that this has no function,
	 * as it is an experimental packet type.
	 * @author Vlad Firoiu
	 */
	protected class StringProcessor implements PacketProcessor {
		protected byte pkt_type, type;
		protected int arg1, arg2;
		
		public byte getPacketType(){return pkt_type;}
		public byte getType(){return type;}
		public int getArg1(){return arg1;}
		public int getArg2(){return arg2;}
		
		protected void readPacket(ByteBufferWrap in) {
			pkt_type = in.getByte();
			type = in.getByte();
			arg1 = in.getUnsignedShort();
			arg2 = in.getUnsignedShort();
		}
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) {
			readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + this.toString());
		}
		
		@Override
		public String toString() {
			return "String Packet\npacket type = " + pkt_type +
					"\ntype = " + type +
					"\narg1 = " + arg1 + 
					"\narg2 = " + arg2;
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate string
	 * processor is to be used.
	 * @return A new StringProcessor object.
	 */
	protected PacketProcessor getStringProcessor(){return new StringProcessor();}
	
	/**
	 * Processes wormhole packets.
	 * TODO: Implement handling of wormholes.
	 * @author Vlad Firoiu
	 * @since xpilot version 4.5.0
	 */
	protected class WormholeProcessor implements PacketProcessor
	{
		protected byte pkt_type;
		protected short x, y;

		public byte getPacketType(){return pkt_type;}
		public short getX(){return x;}
		public short getY(){return y;}
		
		/**
		 * Reads the packet type, x and y.
		 * @param in The buffer to read from.
		 */
		protected void readPacket(ByteBufferWrap in) {
			pkt_type = in.getByte();
			x = in.getShort();
			y = in.getShort();
		}
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client)
				throws PacketReadException {
			readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + this.toString());
		}
		
		@Override
		public String toString() {
			return "Wormhole Packet\ntype = " + pkt_type +
					"\nx = " + x +
					"\ny = " + y;
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate wormhole
	 * processor is to be used.
	 * @return A new WormholeProcessor object.
	 * @since xpilot version 4.5.0
	 */
	protected PacketProcessor getWormholeProcessor(){return new WormholeProcessor();}
	
	/**
	 * Processes asteroid packets.
	 * TODO: Implement handling of asteroids.
	 * @author Vlad Firoiu
	 * @since xpilot version 4.4.0
	 */
	protected class AsteroidProcessor implements PacketProcessor
	{
		protected byte pkt_type, 
		type_size, type, 
		size, rot;
		protected short x, y;
		
		public byte getPacketType(){return pkt_type;}
		public byte getType(){return type;}
		public byte getSize(){return size;}
		public byte getRot(){return rot;}
		public short getX(){return x;}
		public short getY(){return y;}
		
		/**
		 * Reads the packet type, x, y, type, size, and  rotation.
		 * @param in The buffer to read from.
		 */
		protected void readPacket(ByteBufferWrap in) {
			pkt_type = in.getByte();
			x = in.getShort();
			y = in.getShort();
			type_size = in.getByte();
			rot = in.getByte();
			
			type = (byte) ((type_size >> 4) & 0x0F);
			size = (byte) (type_size & 0x0F);
		}

		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client)
				throws PacketReadException {
			readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + this.toString());
		}
		
		@Override
		public String toString() {
			return "Asteroid Packet\npacket type = " + type +
					"\nx = " + x +
					"\ny = " + y +
					"\ntype = " + type +
					"\nsize = " + size +
					"\nrot = " + rot;
		}
	}
	/**
	 * Note that subclasses should override this method if a separate asteroid
	 * processor is to be used.
	 * @return A new AsteroidProcessor object.
	 * @since xpilot version 4.4.0
	 */
	protected PacketProcessor getAsteroidProcessor(){return new AsteroidProcessor();}
	
	/**
	 * Processes lose item packets.
	 * TODO: Implement handling of lose items.
	 * @author Vlad Firoiu
	 */
	protected class LoseItemProcessor implements PacketProcessor
	{
		protected byte pkt_type;
		protected short lose_item;//unsigned byte
		
		public byte getPacketType(){return pkt_type;}
		public short getLoseItem(){return lose_item;}

		protected void readPacket(ByteBufferWrap in) {
			pkt_type = in.getByte();
			lose_item = in.getUnsignedByte();
		}
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client)
				throws PacketReadException {
			readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n'+this.toString());
		}
		
		public String toString() {
			return "Lose Item Packet\npacket type = " + pkt_type +
					"\nlose item = " + lose_item;
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate lose
	 * item processor is to be used.
	 * @return A new LoseItemProcessor object.
	 */
	protected PacketProcessor getLoseItemProcessor(){return new LoseItemProcessor();}
	
	/**
	 * Processes round delay packets.
	 * TODO: Implement handling of round delay.
	 * @author Vlad Firoiu
	 */
	protected class RoundDelayProcessor implements PacketProcessor
	{
		protected byte pkt_type;
		protected short count, max;
		
		public byte getPacketType(){return pkt_type;}
		protected short getCount(){return count;}
		public short getMax(){return max;}
		
		protected void readPacket(ByteBufferWrap in) {
			pkt_type = in.getByte();
			count = in.getShort();
			max = in.getShort();
		}
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client)
				throws PacketReadException {
			readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + this.toString());
		}
		
		@Override
		public String toString() {
			return "Round Delay Packet\npacket type = " + pkt_type +
					"\ncount = " + count +
					"\nmax = " + max;
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate round
	 * delay processor is to be used.
	 * @return A new RoundDelayProcessor object.
	 */
	protected PacketProcessor getRoundDelayProcessor(){return new RoundDelayProcessor();}
	
	/**
	 * Processes phasing time packets.
	 * TODO: Implement handling of phasing time.
	 * @author Vlad Firoiu
	 */
	protected class PhasingTimeProcessor implements PacketProcessor
	{
		protected byte pkt_type;
		protected short count, max;
		
		public byte getPacketType(){return pkt_type;}
		protected short getCount(){return count;}
		public short getMax(){return max;}
		
		protected void readPacket(ByteBufferWrap in) {
			pkt_type = in.getByte();
			count = in.getShort();
			max = in.getShort();
		}
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client)
				throws PacketReadException {
			readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + this.toString());
		}
		
		@Override
		public String toString() {
			return "Phasing Time Packet\npacket type = " + pkt_type +
					"\ncount = " + count +
					"\nmax = " + max;
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate phasing
	 * time processor is to be used.
	 * @return A new PhasingTimeProcessor object.
	 */
	protected PacketProcessor getPhasingTimeProcessor(){return new PhasingTimeProcessor();}
	
	/**
	 * Processes thrust time packets.
	 * TODO: Implement handling of thrust time.
	 * @author Vlad Firoiu
	 */
	protected class ThrustTimeProcessor implements PacketProcessor
	{
		protected byte pkt_type;
		protected short count, max;
		
		public byte getPacketType(){return pkt_type;}
		protected short getCount(){return count;}
		public short getMax(){return max;}
		
		protected void readPacket(ByteBufferWrap in) {
			pkt_type = in.getByte();
			count = in.getShort();
			max = in.getShort();
		}
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client)
				throws PacketReadException {
			readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + this.toString());
		}
		
		@Override
		public String toString() {
			return "Thrust Time Packet\npacket type = " + pkt_type +
					"\ncount = " + count +
					"\nmax = " + max;
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate thrust
	 * time processor is to be used.
	 * @return A new ThrustTimeProcessor object.
	 */
	protected PacketProcessor getThrustTimeProcessor(){return new ThrustTimeProcessor();}
	
	/**
	 * Processes shield time packets.
	 * TODO: Implement handling of shield time.
	 * @author Vlad Firoiu
	 */
	protected class ShieldTimeProcessor implements PacketProcessor
	{
		protected byte pkt_type;
		protected short count, max;
		
		public byte getPacketType(){return pkt_type;}
		protected short getCount(){return count;}
		public short getMax(){return max;}
		
		protected void readPacket(ByteBufferWrap in) {
			pkt_type = in.getByte();
			count = in.getShort();
			max = in.getShort();
		}
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client)
				throws PacketReadException {
			readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + this.toString());
		}
		
		@Override
		public String toString() {
			return "Shield Time Packet\npacket type = " + pkt_type +
					"\ncount = " + count +
					"\nmax = " + max;
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate shield
	 * time processor is to be used.
	 * @return A new ShieldTimeProcessor object.
	 */
	protected PacketProcessor getShieldTimeProcessor(){return new ShieldTimeProcessor();}
	
	/**
	 * Processes MOTD (Message of the Day) packets.
	 * @TODO: Implement handling of motd.
	 * @author Vlad Firoiu
	 */
	protected class MOTDProcessor implements PacketProcessor
	{
		public static final int LENGTH = 1 + 4 + 2 + 4;//11
		
		protected byte pkt_type;
		protected int offset, size;
		protected short length;
		protected String motd;
		
		public byte getPacketType(){return pkt_type;}
		public int getOffset(){return offset;}
		public short getLength(){return length;}
		public int getSize(){return size;}
		public String getMOTD(){return motd;}
		
		protected void readPacket(ByteBufferWrap in) throws ReliableReadException {
			in.setReading();
			if(in.remaining()<=LENGTH) throw reliableReadException;
			pkt_type = in.getByte();
			offset = in.getInt();
			length = in.getShort();
			size = in.getInt();
			
			try {
				motd = in.getString();
			} catch(StringReadException e) {
				throw reliableReadException;
			}
		}
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client)
				throws ReliableReadException {
			readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + this.toString());
		}
		
		@Override
		public String toString() {
			return "MOTD Packet\npacket type = " + pkt_type +
					"\noffset = " + offset +
					"\nlength = " + length +
					"\nsize = " + size +
					"\nmotd = " + motd;
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate motd
	 * processor is to be used.
	 * @return A new MOTDProcessor object.
	 */
	protected PacketProcessor getMOTDProcessor(){return new MOTDProcessor();}
	
	/**
	 * Processes radar packets.
	 * TODO: Implement handling of radar.
	 * @author Vlad Firoiu
	 */
	protected class RadarProcessor extends RadarHolder implements PacketProcessor
	{
		protected byte pkt_type;
		
		public byte getPacketType(){return pkt_type;}
		
		/**
		 * Reads the packet type, x, y, and size.
		 * @param in The buffer to read from.
		 */
		protected void readPacket(ByteBufferWrap in) {
			pkt_type = in.getByte();
			x = in.getShort();
			y = in.getShort();
			size= in.getUnsignedByte();
			
			//x = (int)((double)(x * 256) / Setup->width + 0.5);
			//y = (int)((double)(y * RadarHeight) / Setup->height + 0.5);
		}
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client)
				throws PacketReadException {
			readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + this.toString());
			client.handleRadar(this);
		}
		
		@Override
		public String toString() {
			return "Radar Packet\npacket type = " + pkt_type +
					"\nx = " + x +
					"\ny = " + y +
					"\nsize = " + size;
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate radar
	 * processor is to be used.
	 * @return A new RadarProcessor object.
	 */
	protected PacketProcessor getRadarProcessor(){return new RadarProcessor();}

	/**
	 * Processes destruct packets.
	 * TODO: Implement handling of destruct.
	 * @author Vlad Firoiu
	 */
	protected class DestructProcessor implements PacketProcessor
	{
		protected byte pkt_type;
		protected short count;
		
		public byte getPacketType(){return pkt_type;}
		public short getCount(){return count;}
		
		protected void readPacket(ByteBufferWrap in) {
			pkt_type = in.getByte();
			count = in.getShort();
		}
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client)
				throws PacketReadException {
			readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + this.toString());
		}
		
		@Override
		public String toString() {
			return "Destruct Packet\npacket type = " + pkt_type +
					"\ncount = " + count;
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate destruct
	 * processor is to be used.
	 * @return A new DestructProcessor object.
	 */	
	protected PacketProcessor getDestructProcessor(){return new DestructProcessor();}
	
	/**
	 * Processes shutdown packets.
	 * @author Vlad Firoiu
	 */
	protected class ShutdownProcessor extends ShutdownPacket implements PacketProcessor
	{
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client)
				throws PacketReadException {
			readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + super.toString());
			client.handleShutdown(this);
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate shutdown
	 * processor is to be used.
	 * @return A new ShutdownProcessor object.
	 */	
	protected PacketProcessor getShutdownProcessor(){return new ShutdownProcessor();}
	
	/**
	 * Processes trans packets.
	 * TODO: Implement handling of trans.
	 * @author Vlad Firoiu
	 */
	protected class TransProcessor implements PacketProcessor {
		protected byte pkt_type;
		protected short x1, y1, x2, y2;
		
		public byte getPacketType() {return pkt_type;}
		public short getX1(){return x1;}
		public short getY1(){return y1;}
		public short getX2(){return x2;}
		public short getY2(){return y2;}
		
		public void readPacket(ByteBufferWrap in) {
			pkt_type = in.getByte();
			x1 = in.getShort();
			y1 = in.getShort();
			x2 = in.getShort();
			y2 = in.getShort();
		}
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client)
				throws PacketReadException {
			readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + this.toString());
		}

		@Override
		public String toString() {
			return "Trans Packet\npacket type = " + pkt_type +
					"\nx1 = " + x1 +
					"\ny1 = " + y2 +
					"\nx2 = " + x2 +
					"\ny2 = " + y2;
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate trans
	 * processor is to be used.
	 * @return A new TransProcessor object.
	 */	
	protected PacketProcessor getTransProcessor(){return new TransProcessor();}
	
	/**
	 * Processes audio packets.
	 * TODO: Implement handling of audio.
	 * @author Vlad Firoiu
	 */
	protected class AudioProcessor implements PacketProcessor {
		protected byte pkt_type, type, volume;
		
		public byte getPacketType(){return pkt_type;}
		public byte getType(){return type;}
		public byte getVolume(){return volume;}
		
		public void readPacket(ByteBufferWrap in) {
			pkt_type = in.getByte();
			type = in.getByte();
			volume = in.getByte();
		}
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client)
				throws PacketReadException {
			readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + this.toString());
		}
		
		@Override
		public String toString() {
			return "Audio Packet\npacket type = " + pkt_type +
					"\ntype = " + type +
					"\nvolume = " + volume;
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate audio
	 * processor is to be used.
	 * @return A new AudioProcessor object.
	 */	
	protected PacketProcessor getAudioProcessor(){return new AudioProcessor();}
	
	/**
	 * Processes time left packets.
	 * TODO: Implement handling of time left.
	 * @author Vlad Firoiu
	 */
	protected class TimeLeftProcessor implements PacketProcessor {
		protected byte pkt_type;
		protected int seconds;
		
		public byte getPacketType(){return pkt_type;}
		public int getSeconds(){return seconds;}
		
		public void readPacket(ByteBufferWrap in) {
			pkt_type = in.getByte();
			seconds = in.getInt();
		}

		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client)
				throws PacketReadException {
			readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + this.toString());
		}
		
		@Override
		public String toString() {
			return "Time Left Packet\npacket type = " + pkt_type +
					"\nseconds = " + seconds;
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate time
	 * left processor is to be used.
	 * @return A new {@code TimeLeftProcessor} object.
	 */	
	protected PacketProcessor getTimeLeftProcessor(){return new TimeLeftProcessor();}
}
