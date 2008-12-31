package net.sf.jxpilot.net;

import static net.sf.jxpilot.JXPilot.PRINT_PACKETS;
import static net.sf.jxpilot.net.Ack.putAck;
import static net.sf.jxpilot.net.packet.Packet.*;
import static net.sf.jxpilot.net.packet.ReplyData.readReplyData;

import java.io.*;
import java.net.*;
import java.nio.channels.DatagramChannel;

import javax.swing.JOptionPane;

import net.sf.jxpilot.AbstractClient;
import net.sf.jxpilot.data.Keys;
import net.sf.jxpilot.game.*;
import net.sf.jxpilot.map.BlockMap;
import net.sf.jxpilot.map.BlockMapSetup;
import net.sf.jxpilot.net.packet.*;
import net.sf.jxpilot.util.BitVector;
import net.sf.jxpilot.util.HolderList;
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
	private final PacketProcessor[] processors = new PacketProcessor[256];
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
		processors[PKT_EYES]		= getEyesProcessor();
		processors[PKT_TIME_LEFT]	= getTimeLeftProcessor();
		processors[PKT_AUDIO]		= getAudioProcessor();
		processors[PKT_START]		= getStartProcessor();
		processors[PKT_END]			= getEndProcessor();
		processors[PKT_SELF]		= getSelfProcessor();
		processors[PKT_DAMAGED]		= getDamagedProcessor();
		processors[PKT_CONNECTOR]	= getConnectorProcessor();
		processors[PKT_LASER]		= getLaserProcessor();
		processors[PKT_REFUEL]		= getRefuelProcessor();
		processors[PKT_SHIP]		= getShipProcessor();
		processors[PKT_ECM]			= getECMProcessor();
		processors[PKT_TRANS]		= getTransProcessor();
		processors[PKT_PAUSED]		= getPausedProcessor();
		processors[PKT_ITEM]		= getItemProcessor();
		processors[PKT_MINE]		= getMineProcessor();
		processors[PKT_BALL]		= getBallProcessor();
		processors[PKT_MISSILE]		= getMissileProcessor();
		processors[PKT_SHUTDOWN]	= getShutdownProcessor();
		processors[PKT_DESTRUCT]	= getDestructProcessor();
		processors[PKT_SELF_ITEMS]	= getSelfItemsProcessor();
		processors[PKT_FUEL]		= getFuelProcessor();
		processors[PKT_CANNON]		= getCannonProcessor();
		processors[PKT_TARGET]		= getTargetProcessor();
		processors[PKT_RADAR]		= getRadarProcessor();
		processors[PKT_FASTRADAR]	= getFastRadarProcessor();
		processors[PKT_RELIABLE]	= getReliableProcessor();
		processors[PKT_QUIT]		= getQuitProcessor();
		processors[PKT_MODIFIERS]  	= getModifiersProcessor();
		processors[PKT_FASTSHOT]	= getFastShotProcessor();
		processors[PKT_THRUSTTIME] 	= getThrustTimeProcessor();
		processors[PKT_SHIELDTIME] 	= getShieldTimeProcessor();
		processors[PKT_PHASINGTIME]	= getPhasingTimeProcessor();
		processors[PKT_ROUNDDELAY] 	= getRoundDelayProcessor();
		processors[PKT_LOSEITEM]	= getLoseItemProcessor();
		processors[PKT_WRECKAGE]	= getWreckageProcessor();
		processors[PKT_ASTEROID]	= getAsteroidProcessor();
		processors[PKT_WORMHOLE]	= getWormholeProcessor();
		
		PacketProcessor debrisProcessor = getDebrisProcessor();	
		int pkt_debris = Utilities.getUnsignedByte(PKT_DEBRIS);
		for (int i = 0;i<DEBRIS_TYPES;i++) {
			processors[i+pkt_debris] = debrisProcessor;
		}
		
		//reliable types
		processors[PKT_MOTD]		= getMOTDProcessor();
		processors[PKT_MESSAGE]		= getMessageProcessor();
		processors[PKT_TEAM_SCORE]	= getTeamScoreProcessor();
		processors[PKT_PLAYER]		= getPlayerProcessor();
		processors[PKT_SCORE]		= getScoreProcessor();
		processors[PKT_TIMING]		= getTimingProcessor();
		processors[PKT_LEAVE]		= getLeaveProcessor();
		processors[PKT_WAR]			= getWarProcessor();
		processors[PKT_SEEK]		= getSeekProcessor();
		processors[PKT_BASE]		= getBaseProcessor();
		processors[PKT_QUIT]		= getQuitProcessor();
		processors[PKT_STRING]		= getStringProcessor();
		processors[PKT_SCORE_OBJECT]= getScoreObjectProcessor();
		processors[PKT_TALK_ACK]	= getTalkAckProcessor();
		processors[PKT_REPLY]		= getReplyProcessor();
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
			
			if (processors[type]!=null)
			{
				try
				{
					processors[type].processPacket(in, client);
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

			if(processors[type]!=null)
			{
				int pos = reliableBuf.position();//stores reliable buffer's position
				try
				{
					processors[type].processPacket(reliableBuf, client);
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
			putTurnSpeed(out, new_speed);
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
		protected final BasePacket basePacket = new BasePacket();
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
		protected final DebrisPacket debrisPacket = new DebrisPacket();
		protected final AbstractDebrisHolder debrisHolder = new AbstractDebrisHolder();
		
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

	/**
	 * Processes self items packets.
	 * @author Vlad Firoiu
	 */
	protected class SelfItemsProcessor implements PacketProcessor {
		protected final SelfItemsPacket selfItemsPacket = new SelfItemsPacket();
		
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			selfItemsPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + selfItemsPacket.toString());
			client.handleSelfItems(selfItemsPacket.getItems());
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate self items
	 * processor is to be used.
	 * @return A new {@code SelfItemsProcessor} object.
	 */
	protected PacketProcessor getSelfItemsProcessor(){return new SelfItemsProcessor();}
	
	/**
	 * Processes self packets.
	 * @author Vlad Firoiu
	 */
	protected class SelfProcessor implements PacketProcessor {
		protected final SelfPacket selfPacket = new SelfPacket();
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			selfPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + selfPacket.toString());
			client.handleSelf(selfPacket);
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate self
	 * processor is to be used.
	 * @return A new {@code SelfProcessor} object.
	 */
	protected PacketProcessor getSelfProcessor(){return new SelfProcessor();}
	
	/**
	 * Processes modifiers packets.
	 * @author Vlad Firoiu
	 */
	protected class ModifiersProcessor implements PacketProcessor {
		protected final ModifiersPacket modifiersPacket = new ModifiersPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			modifiersPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + modifiersPacket.toString());
			client.handleModifiers(modifiersPacket.getModifiers());
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate modifiers
	 * processor is to be used.
	 * @return A new {@code ModifiersProcessor} object.
	 */
	protected PacketProcessor getModifiersProcessor(){return new ModifiersProcessor();}	
	
	/**
	 * Processes end packets.
	 * @author Vlad Firoiu
	 */
	protected class EndProcessor implements PacketProcessor {
		protected final EndPacket endPacket = new EndPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			endPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + endPacket.toString());
			client.handleEnd(endPacket.getLoops());
		}
	}

	/**
	 * Note that subclasses should override this method if a separate end
	 * processor is to be used.
	 * @return A new {@code EndProcessor} object.
	 */
	protected PacketProcessor getEndProcessor(){return new EndProcessor();}	
	
	/**
	 * Processes ball packets.
	 * @author Vlad Firoiu
	 */
	protected class BallProcessor implements PacketProcessor {
		protected final BallPacket ballPacket = new BallPacket();
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			ballPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + ballPacket.toString());
			client.handleBall(ballPacket);
		}
	}

	/**
	 * Note that subclasses should override this method if a separate ball
	 * processor is to be used.
	 * @return A new {@code BallProcessor} object.
	 */
	protected PacketProcessor getBallProcessor(){return new BallProcessor();}
	
	/**
	 * Processes Ship packets.
	 * @author Vlad Firoiu
	 */
	protected class ShipProcessor implements PacketProcessor {
		protected final ShipPacket shipPacket = new ShipPacket();
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			shipPacket.readPacket(in);
			if(PRINT_PACKETS)System.out.println('\n' + shipPacket.toString());
			client.handleShip(shipPacket);
		}
	}

	/**
	 * Note that subclasses should override this method if a separate ship
	 * processor is to be used.
	 * @return A new {@code ShipProcessor} object.
	 */
	protected PacketProcessor getShipProcessor(){return new ShipProcessor();}
	
	/**
	 * Processes Fast Shot packets.
	 * @author Vlad Firoiu
	 */
	protected class FastShotProcessor implements PacketProcessor {
		protected final FastShotPacket fastShotPacket = new FastShotPacket();
		protected final AbstractDebrisHolder fastShotHolder = new AbstractDebrisHolder();
		
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			fastShotPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + fastShotPacket.toString());
			for(int i = 0;i<fastShotPacket.getNum();i++) {
				client.handleFastShot(fastShotHolder.setAbstractDebris(fastShotPacket.getType(), in.getUnsignedByte(), in.getUnsignedByte()));
			}
		}
	}

	/**
	 * Note that subclasses should override this method if a separate fast shot
	 * processor is to be used.
	 * @return A new {@code FastShotProcessor} object.
	 */
	protected PacketProcessor getFastShotProcessor(){return new FastShotProcessor();}
	
	/**
	 * Processes Item packets.
	 * @author Vlad Firoiu
	 */
	protected class ItemProcessor implements PacketProcessor {
		protected final ItemPacket itemPacket = new ItemPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			itemPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + itemPacket.toString());
			//TODO: Implement handling of item packets.
		}
	}

	/**
	 * Note that subclasses should override this method if a separate item
	 * processor is to be used.
	 * @return A new {@code ItemProcessor} object.
	 */
	protected PacketProcessor getItemProcessor(){return new ItemProcessor();}
	
	/**
	 * Processes Fast Radar packets.
	 * @author Vlad Firoiu
	 */
	protected class FastRadarProcessor implements PacketProcessor {
		protected final FastRadarPacket fastRadarPacket = new FastRadarPacket();
		
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			fastRadarPacket.clear();
			fastRadarPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + fastRadarPacket.toString());
			HolderList<RadarHolder> radarHolders = fastRadarPacket.getRadarHolders();
			for(int i = 0;i<radarHolders.size();i++) {
				client.handleRadar(radarHolders.get(i));
			}
		}
	}

	/**
	 * Note that subclasses should override this method if a separate fast radar
	 * processor is to be used.
	 * @return A new {@code FastRadarProcessor} object.
	 */
	protected PacketProcessor getFastRadarProcessor(){return new FastRadarProcessor();}
	
	/**
	 * Processes data from a Paused packet.
	 * @author Vlad Firoiu
	 */
	protected class PausedProcessor implements PacketProcessor {
		protected final PausedPacket pausedPacket = new PausedPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			pausedPacket.readPacket(in);
			if(PRINT_PACKETS)System.out.println('\n' + pausedPacket.toString());
			//TODO: Implement handling of Paused packets.
		}
	}

	/**
	 * Note that subclasses should override this method if a separate paused
	 * processor is to be used.
	 * @return A new {@code PausedProcessor} object.
	 */
	protected PacketProcessor getPausedProcessor(){return new WreckageProcessor();}
	
	/**
	 * Processes Wreckage packets.
	 * @author Vlad Firoiu
	 */
	protected class WreckageProcessor implements PacketProcessor {
		protected final WreckagePacket wreckagePacket = new WreckagePacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			wreckagePacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + wreckagePacket.toString());
			//TODO: Implement handling of Wreckage packets.
		}
	}

	/**
	 * Note that subclasses should override this method if a separate wreckage
	 * processor is to be used.
	 * @return A new {@code WreckageProcessor} object.
	 */
	protected PacketProcessor getWreckageProcessor(){return new WreckageProcessor();}
	
	/**
	 * Processes War packets.
	 * @author Vlad Firoiu
	 */
	protected class WarProcessor implements PacketProcessor {
		protected final WarPacket warPacket = new WarPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException {
			warPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + warPacket.toString());
			//TODO: Implement handling of War packets.
		}
	};

	/**
	 * Note that subclasses should override this method if a separate war
	 * processor is to be used.
	 * @return A new {@code WarProcessor} object.
	 */
	protected PacketProcessor getWarProcessor(){return new WarProcessor();}
	
	/**
	 * Processes Connector packets.
	 * @author Vlad Firoiu
	 */
	protected class ConnectorProcessor implements PacketProcessor {
		protected final ConnectorPacket connectorPacket = new ConnectorPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			connectorPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + connectorPacket.toString());
			client.handleConnector(connectorPacket);
		}
	}

	/**
	 * Note that subclasses should override this method if a separate connector
	 * processor is to be used.
	 * @return A new {@code ConnectorProcessor} object.
	 */
	protected PacketProcessor getConnectorProcessor(){return new ConnectorProcessor();}
	
	/**
	 * Processes Leave packets.
	 * @author Vlad Firoiu
	 */
	protected class LeaveProcessor implements PacketProcessor {
		protected final LeavePacket leavePacket = new LeavePacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException {
			leavePacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + leavePacket.toString());
			client.handleLeave(leavePacket.getId());
		}
	}

	/**
	 * Note that subclasses should override this method if a separate leave
	 * processor is to be used.
	 * @return A new {@code LeaveProcessor} object.
	 */
	protected PacketProcessor getLeaveProcessor(){return new LeaveProcessor();}
	
	/**
	 * Processes data from a Score Object packet.
	 * @author Vlad Firoiu
	 */
	protected class ScoreObjectProcessor implements PacketProcessor {
		protected final ScoreObjectPacket scoreObjectPacket = new ScoreObjectPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException {
			scoreObjectPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + scoreObjectPacket.toString());
			client.handleScoreObject(scoreObjectPacket);
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate score object
	 * processor is to be used.
	 * @return A new {@code ScoreObjectProcessor} object.
	 */
	protected PacketProcessor getScoreObjectProcessor(){return new ScoreObjectProcessor();}
	
	/**
	 * Processes Mine packets.
	 * @author Vlad Firoiu
	 */
	protected class MineProcessor implements PacketProcessor {
		protected final MinePacket minePacket = new MinePacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			minePacket.readPacket(in);
			if(PRINT_PACKETS)System.out.println('\n' + minePacket.toString());
			client.handleMine(minePacket);
		}
	}
	
	/**
	 * Note that subclasses should override this method if a separate mine
	 * processor is to be used.
	 * @return A new {@code MineProcessor} object.
	 */
	protected PacketProcessor getMineProcessor(){return new MineProcessor();}
	
	/**
	 * Processes Cannon packets.
	 * @author Vlad Firoiu
	 */
	protected class CannonProcessor implements PacketProcessor {
		protected final CannonPacket cannonPacket = new CannonPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			cannonPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + cannonPacket.toString());
			
			out.putByte(PKT_ACK_CANNON);
			out.putInt(last_loops);
			out.putShort((short)cannonPacket.getNum());
			
			client.handleCannon(cannonPacket);
		}
	}

	/**
	 * Note that subclasses should override this method if a separate cannon
	 * processor is to be used.
	 * @return A new {@code CannonProcessor} object.
	 */
	protected PacketProcessor getCannonProcessor(){return new CannonProcessor();}
	
	/**
	 * Processes Fuel packets.
	 * @author Vlad Firoiu
	 */
	protected class FuelProcessor implements PacketProcessor {
		protected final FuelPacket fuelPacket = new FuelPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			fuelPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + fuelPacket.toString());
			
			out.putByte(PKT_ACK_FUEL);
			out.putInt(last_loops);
			out.putShort((short)fuelPacket.getNum());
			
			client.handleFuel(fuelPacket);
		}
	}

	/**
	 * Note that subclasses should override this method if a separate fuel
	 * processor is to be used.
	 * @return A new {@code FuelProcessor} object.
	 */
	protected PacketProcessor getFuelProcessor(){return new FuelProcessor();}
	
	/**
	 * Processes Missile packets.
	 * @author Vlad Firoiu
	 */
	protected class MissileProcessor implements PacketProcessor {
		protected final MissilePacket missilePacket = new MissilePacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			missilePacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + missilePacket.toString());
			client.handleMissile(missilePacket);
		}
	}

	/**
	 * Note that subclasses should override this method if a separate missile
	 * processor is to be used.
	 * @return A new {@code MissileProcessor} object.
	 */
	protected PacketProcessor getMissileProcessor(){return new MissileProcessor();}
	
	/**
	 * Processes Damaged packets.
	 * @author Vlad Firoiu
	 */
	protected class DamagedProcessor implements PacketProcessor {
		protected final DamagedPacket damagedPacket = new DamagedPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			damagedPacket.readPacket(in);
			if (PRINT_PACKETS) System.out.println('\n' + damagedPacket.toString());
			//TODO: Implement handling of Damaged packets.
		}
	}

	/**
	 * Note that subclasses should override this method if a separate damaged
	 * processor is to be used.
	 * @return A new {@code DamagedProcessor} object.
	 */
	protected PacketProcessor getDamagedProcessor(){return new DamagedProcessor();}
	
	/**
	 * Processes Laser packets.
	 * @author Vlad Firoiu
	 */
	protected class LaserProcessor implements PacketProcessor {
		protected final LaserPacket laserPacket = new LaserPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			laserPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + laserPacket.toString());
			client.handleLaser(laserPacket);
		}
	}

	/**
	 * Note that subclasses should override this method if a separate laser
	 * processor is to be used.
	 * @return A new {@code LaserProcessor} object.
	 */
	protected PacketProcessor getLaserProcessor(){return new LaserProcessor();}
	
	/**
	 * Processes ECM packets.
	 * @author Vlad Firoiu
	 */
	protected class ECMProcessor implements PacketProcessor {
		protected final ECMPacket ecmPacket = new ECMPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			ecmPacket.readPacket(in);
			if (PRINT_PACKETS) System.out.println('\n' + ecmPacket.toString());
			client.handleECM(ecmPacket);
		}
	}

	/**
	 * Note that subclasses should override this method if a separate ECM
	 * processor is to be used.
	 * @return A new {@code ECMProcessor} object.
	 */
	protected PacketProcessor getECMProcessor(){return new ECMProcessor();}
	
	/**
	 * Processes Refuel packets.
	 * @author Vlad Firoiu
	 */
	protected class RefuelProcessor implements PacketProcessor {
		protected final RefuelPacket refuelPacket = new RefuelPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws PacketReadException {
			refuelPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + refuelPacket.toString());
			client.handleRefuel(refuelPacket);
		}
	}

	/**
	 * Note that subclasses should override this method if a separate refuel
	 * processor is to be used.
	 * @return A new {@code RefuelProcessor} object.
	 */
	protected PacketProcessor getRefuelProcessor(){return new RefuelProcessor();}
	
	/**
	 * Processes Talk Ack packets.
	 * @author Vlad Firoiu
	 */
	protected class TalkAckProcessor implements PacketProcessor {
		protected final TalkAckPacket talkAckPacket = new TalkAckPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException {
			talkAckPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + talkAckPacket.toString());
			if (talkAckPacket.getTalkAck() >= talk_pending) {
				talk_pending = 0;
			}
		}
	}

	/**
	 * Note that subclasses should override this method if a separate talk ack
	 * processor is to be used.
	 * @return A new {@code TalkAckProcessor} object.
	 */
	protected PacketProcessor getTalkAckProcessor(){return new TalkAckProcessor();}
	
	/**
	 * Processes Timing packets.
	 * @author Vlad Firoiu
	 */
	protected class TimingProcessor implements PacketProcessor {
		protected final TimingPacket timingPacket = new TimingPacket();
		@Override
		public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException {
			timingPacket.readPacket(in);
			if(PRINT_PACKETS) System.out.println('\n' + timingPacket.toString());
		}
	}

	/**
	 * Note that subclasses should override this method if a separate timing
	 * processor is to be used.
	 * @return A new {@code TimingProcessor} object.
	 */
	protected PacketProcessor getTimingProcessor(){return new TimingProcessor();}
	
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
			//TODO: Implement handling of Seek packets.
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
