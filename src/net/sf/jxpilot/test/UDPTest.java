package net.sf.jxpilot.test;

import static net.sf.jxpilot.test.Ack.*;
import static net.sf.jxpilot.test.ReplyMessage.*;
import static net.sf.jxpilot.test.Packet.*;
import static net.sf.jxpilot.test.ReplyData.*;
import javax.swing.*;
import java.util.*;
import net.sf.xpilotpanel.XPilotPanel;
import net.sf.xpilotpanel.gui.AboutWindow;
import static net.sf.jxpilot.test.MapError.*;
import static net.sf.jxpilot.test.ReliableDataError.*;
import static net.sf.jxpilot.test.ReliableData.*;
import static net.sf.jxpilot.test.UDPTest.receivePacket;

import java.net.*;
import java.awt.Image;
import java.io.*;
import java.nio.channels.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.HashMap;

public class UDPTest {

	public static final int MAX_PACKET_SIZE = 65507;
	public static final int MAGIC =0x4401F4ED;
	public static final short SERVER_MAIN_PORT = 15345;
	//public static final short CLIENT_PORT = 15345;
	
	public static final String LKRAUSS_ADDRESS = "213.239.204.35";
	public static final String STROWGER_ADDRESS = "149.156.203.245";
	public static final String CHAOS_ADDRESS = "129.13.108.207";
	public static final String LOCAL_LOOPBACK_ADDRESS = "127.0.0.1";
	public static final String SERVER_IP_ADDRESS = STROWGER_ADDRESS;
	
	public static final byte END_OF_STRING = 0x00;
	
	public static final byte ENTER_QUEUE_pack =	0x01;
	public static final byte ENTER_GAME_pack = 	0x00;
	public static final byte SUCCESS = 			0x00;
	
	//client variables
	public static final int TEAM = 0x0000FFFF;
	public static final String DISPLAY = "";
	public static final String REAL_NAME = "TEST";
	public static final String NICK = "test";
	public static final String HOST = "java";
	public static final short POWER = 55;
	public static final short TURN_SPEED = 16;
	public static final short TURN_RESISTANCE = 0;
	public static final byte MAX_FPS = (byte)0x255;
	public static final byte[] MOTD_BYTES = {0,0,0x47,2,0,0x43,3};
	
	//used for sending display
	public static final short WIDTH_WANTED = 0x400;
	public static final short HEIGHT_WANTED = 0x400;
	public static final byte NUM_SPARK_COLORS = 0x08;
	public static final byte SPARK_RAND = 0x33;
	
	private static final ShipShape SHIP = ShipShape.defaultShip;
	
    /**
     * JXPilot's windows icon.
     */
    private static Image icon = null;

    // client objects/variables
	private static InetSocketAddress server_address;
	private static DatagramSocket socket;
	private static DatagramChannel channel;
	private static ByteBuffer out = ByteBuffer.allocate(MAX_PACKET_SIZE);
	private static ByteBuffer in = ByteBuffer.allocate(MAX_PACKET_SIZE);
	private static ByteBuffer map = ByteBuffer.allocate(MAX_PACKET_SIZE);
	private static MapSetup setup = new MapSetup();
	private static PacketReader[] readers = new PacketReader[256];
	private static ReplyData reply = new ReplyData();
	private static ReliableData reliable = new ReliableData();
	private static ReplyMessage message = new ReplyMessage();
	private static BitVector keyboard = new BitVector(Keys.NUM_KEYS);
	private static int last_keyboard_change=0;
	
	//sets function to handle packets
	static
	{
		readers[PKT_RELIABLE] = new PacketReader()
		{
			public void readPacket(ByteBuffer buf)
			{
				System.out.println(readReliableData(reliable, buf, out));
				//buf.clear();
			}
		};
		
		readers[PKT_QUIT] = new PacketReader()
		{
			public void readPacket(ByteBuffer buf)
			{
				byte type = buf.get();
				String reason = getString(buf);
				System.out.println("Server closed connection: " + reason);
				System.exit(0);
			}
		};
		
		readers[PKT_START] = new PacketReader()
		{
			public void readPacket(ByteBuffer buf)
			{
				byte type = buf.get();
				int loops = buf.get();
				int key_ack = buf.get();
				
				/*
				System.out.println("\nStart Packet :" +
									"\ntype = " + String.format("%x", type) +
									"\nloops = " + loops +
									"\nkey ack = " + key_ack);
				*/
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
		};
		
		readers[PKT_PLAYER] = new PacketReader()
		{
			public void readPacket(ByteBuffer in)
			{
				byte type = in.get();
				short id = in.getShort();
				short myTeam = getUnsignedByte(in.get());
				short myChar = getUnsignedByte(in.get());
				String name = getString(in);
				String real = getString(in);
				String host = getString(in);
				
				System.out.println("\nPlayer Packet\ntype = "  +type+
									"\nid = "  + id +
									"\nmy team = " + myTeam +
									"\nmy char = " + myChar +
									"\n name = " + name +
									"\nreal = " + real +
									"\nhost = " + host);
				
				
				ShipShape shape = ShipShape.parseShip(getString(in), getString(in));
				System.out.println(shape);
			}
			
			/*
			int Receive_player(void)
			{
				int			n;
				short		id;
				u_byte		ch, myteam, mychar;
				char		name[MAX_CHARS],
				real[MAX_CHARS],
				host[MAX_CHARS],
				shape[2*MSG_LEN],
				*cbuf_ptr = cbuf.ptr;

				if ((n = Packet_scanf(&cbuf,
						"%c%hd%c%c" "%s%s%s" "%S",
						&ch, &id, &myteam, &mychar,
						name, real, host,
						shape)) <= 0) {
					return n;
				}
				name[MAX_NAME_LEN - 1] = '\0';
				real[MAX_NAME_LEN - 1] = '\0';
				host[MAX_HOST_LEN - 1] = '\0';
				if (version > 0x3200) {
					if ((n = Packet_scanf(&cbuf, "%S", &shape[strlen(shape)])) <= 0) {
						cbuf.ptr = cbuf_ptr;
						return n;
					}
				}
				if ((n = Handle_player(id, myteam, mychar, name, real, host, shape)) == -1) {
					return -1;
				}
				return 1;
			}
			*/
		};
	}
	
	public static void main(String[] args)
	{	
		/*
        try {
            java.util.Map<String, Object> xpilotpanelEmbeddedParams = new HashMap<String, Object>();
            xpilotpanelEmbeddedParams.put(
                    XPilotPanel.EMBEDDED_PARAMETER_MAIN_WINDOW_TITLE,
                    "JXPilot - XPilotPanel");
            xpilotpanelEmbeddedParams.put(XPilotPanel.EMBEDDED_PARAMETER_ICON,
                    getJXPilotIcon());
            xpilotpanelEmbeddedParams.put(
                    XPilotPanel.EMBEDDED_PARAMETER_CLIENT_LAUNCH_METHOD,
                    UDPTest.class.getDeclaredMethod("runClient", String.class,
                            int.class));

            XPilotPanel.embeddedLaunch(xpilotpanelEmbeddedParams);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
		*/
		
        runClient(SERVER_IP_ADDRESS, SERVER_MAIN_PORT);
        	
			/*
			
			//channel.send(out, address);
			
			
			byte[] b = {(byte)0xF4};
			
			DatagramPacket send = new DatagramPacket(bytes, bytes.length, address);
			DatagramPacket receive = new DatagramPacket(new byte[100], 100);
			
			
			socket.send(send);
			
			socket.receive(receive);
			
			NumberFormat numf = NumberFormat.getIntegerInstance();
			
			byte[] reply = {0x4F,0x15,(byte)0xF4,(byte)0xED,0x31,0x00};
			
			for(int i = 0;i<reply.length;i++)
			{
				System.out.println(reply[i] == receive.getData()[i]);
			}
			*/

	}
	
	public static void runClient(String serverIP, int serverPort)
	{
		try
		{
			try{
				server_address = new InetSocketAddress(serverIP, serverPort);
				
				channel = DatagramChannel.open();
				socket = channel.socket();
				//socket.bind();
				
				//System.out.println(socket.getLocalPort());
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			
			
			
			//creates first message
			//putJoinRequest(out, "vlad", PORT, "Vlad","xxx", 0x0000FFFF);
			
			byte[] first_packet = 
			{0x4F, 0x15, (byte)0xF4, (byte)0xED, 0x76, 0x6C, 0x61, 0x64, 0x00, 0x04, 0x12, 0x31};
			
			//{0x4f, 0x15, (byte)0xf4, (byte)0xed, 0x76, 0x6c, 0x61, 0x64, 0x00, 0x04, 0x13, 0x01, 0x56, 0x6c, 0x61, 0x64, 0x00, 0x00, 0x78, 0x78, 0x78, 0x00, 0x00, 0x00, (byte)0xff, (byte)0xff};

			//4f15f4ed766c616400041301566c61640000787878000000ffff

			/*
			out.flip();
			for(int i=0;i < bytes.length;i++)
			{
				System.out.printf("Buffer : %x Actual %x\n", out.get(), bytes[i]);
			}
			*/
			
			
			
			//channel.send(ByteBuffer.wrap(first_packet), server_address);
			
			//receivePacket(in);
			
			sendJoinRequest(out, REAL_NAME, socket.getLocalPort(), NICK, HOST, TEAM);
			
			getReplyMessage(in, message);
			System.out.println(message);
			
			while(message.getPack()!=ENTER_GAME_pack)
			{
				getReplyMessage(in, message);
				System.out.println(message);
			}
			
			int server_port = message.getValue();
			System.out.println("New server port: "+server_port);
			
			server_address = new InetSocketAddress(serverIP, server_port);
			
			System.out.println("Sending Verify");
			sendVerify(out, REAL_NAME, NICK);
			
			ReliableDataError result=null;
			while (result!=ReliableDataError.NO_ERROR)
			{
				result = getReliableData(reliable, in, out);
				//System.out.println(result);
			}
			
			netSetup(in, map, setup, reliable);
			
			map.flip();
			System.out.println(map.remaining()+"\n\nMap:\n");
			
			// setup.printMapData();
	        //
	        MapFrame frame = new MapFrame(new Map(setup));
	        //
	        frame.setVisible(true);
			
			
			netStart(out);
			
			while(getReliableData(reliable, in, out)!=ReliableDataError.NO_ERROR)
			System.out.println(reliable);
			
			System.out.println(readReplyData(in, reply));
			netPacket(in);
			
			//System.out.println(keyboard.setBits());
			//System.out.println(keyboard.setBit(Keys.KEY_SELF_DESTRUCT, false));
			//System.out.println(keyboard.setBit(Keys.KEY_PAUSE, false));
			keyboard.clearBits();
			keyboard.setBit(Keys.KEY_TURN_RIGHT, true);
			
            while(true)
			{
            	in.clear();
            	receivePacket(in);
        		in.flip();
        		
				netPacket(in);
				keyboard.switchBit(Keys.KEY_FIRE_SHOT);
				keyboard.switchBit(Keys.KEY_THRUST);
				sendKeyboard(out, keyboard);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
					
	}
	
	public static byte peekByte(ByteBuffer buf)
	{
		//return buf.get(0);
		
		byte b = buf.get();
		buf.position(buf.position()-1);
		return b;
	}
	
	//uses 1 byte chars
	public static void putString(ByteBuffer buffer, String str)
	{
		try
		{
			buffer.put(str.getBytes("US-ASCII"));
			if (!str.endsWith(String.valueOf((char)END_OF_STRING)))
			buffer.put(END_OF_STRING);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//uses 1 byte chars
	public static String getString(ByteBuffer buffer)
	{
		StringBuffer b = new StringBuffer();
		byte ch;
		
		do
		{
			ch = buffer.get();
			b.append((char)ch);
		} while(ch != END_OF_STRING);
		return b.toString();
	
	}
	public static int getUnsignedShort(short val)
	{
		return (int)((char)val);
	}
	
	public static short getUnsignedByte(byte val)
	{
		
		short temp = (short)(0xFF & (int)val);
		
		//System.out.println("\nByte was " + val + "\nNow is " + temp);
		
		return temp;
	}
	
	public static void putJoinRequest(ByteBuffer buf, String real_name, int port, String nick, String host, int team)
	{
		buf.clear();
		
		buf.putInt(MAGIC);
		putString(buf, real_name);
		//buf.put(PKT_MESSAGE);
		buf.putShort((short)port);
		
		buf.put(ENTER_QUEUE_pack);
		putString(buf, nick);
		putString(buf, DISPLAY);
		putString(buf, host);
		buf.putInt(team);
	}
	
	public static void sendJoinRequest(ByteBuffer buf, String real_name, int port, String nick, String host, int team)
	{
		buf.clear();
		putJoinRequest(buf, real_name, port, nick, host, team);
		try
		{
			buf.flip();
			channel.send(buf, server_address);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void receivePacket(ByteBuffer buf)
	{
		buf.clear();
		try
		{
			channel.receive(buf);
			//System.out.println("\nBuf received " + buf.position());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static void sendPacket(ByteBuffer buf)
	{
		try{
			channel.send(buf, server_address);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void putVerify(ByteBuffer buf, String real_name, String nick)
	{
		//buf.clear();
		
		buf.put(PKT_VERIFY);
		putString(buf, real_name);
		putString(buf, nick);
		putString(buf, DISPLAY);
	}
	
	private static void sendVerify(ByteBuffer buf, String real_name, String nick)
	{
		buf.clear();
		putVerify(buf, real_name, nick);
		try
		{
			buf.flip();
			channel.send(buf, server_address);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void sendAck(ByteBuffer buf, Ack ack)
	{
		buf.clear();
		putAck(buf, ack);
		try
		{
			buf.flip();
			channel.send(buf, server_address);
			System.out.println("\n"+ack);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void putPower(ByteBuffer buf, short power)
	{
		buf.put(PKT_POWER);
		buf.putShort((short)(power*256));
	}
	private static void putPowerS(ByteBuffer buf, short power)
	{
		buf.put(PKT_POWER_S);
		buf.putShort((short)(power*256));
	}
	
	private static void putTurnSpeed(ByteBuffer buf, short turn_speed)
	{
		buf.put(PKT_TURNSPEED);
		buf.putShort((short)(turn_speed*256));
	}
	private static void putTurnSpeedS(ByteBuffer buf, short turn_speed)
	{
		buf.put(PKT_TURNSPEED_S);
		buf.putShort((short)(turn_speed*256));
	}
	private static void putTurnResistance(ByteBuffer buf, short turn_resistance)
	{
		buf.put(PKT_TURNRESISTANCE);
		buf.putShort((short)(turn_resistance*256));
	}
	private static void putTurnResistanceS(ByteBuffer buf, short turn_resistance)
	{
		buf.put(PKT_TURNRESISTANCE_S);
		buf.putShort((short)(turn_resistance*256));
	}
	private static void putDisplay(ByteBuffer buf)
	{
		buf.put(PKT_DISPLAY);
		buf.putShort(WIDTH_WANTED);
		buf.putShort(HEIGHT_WANTED);
		buf.put(NUM_SPARK_COLORS);
		buf.put(SPARK_RAND);
	}
	private static void putMOTDRequest(ByteBuffer buf)
	{
		buf.put(PKT_MOTD);
		buf.put(MOTD_BYTES);
	}
	
	private static void putFPSRequest(ByteBuffer buf, byte max_fps)
	{
		buf.put(PKT_ASYNC_FPS);
		buf.put(max_fps);
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
	private static void netStart(ByteBuffer out)
	{
		out.clear();
		
		out.put(PKT_SHAPE);
		putString(out, SHIP.toString());
		
		out.put(PKT_PLAY);
		putPower(out, POWER);
		putPowerS(out, POWER);
		putTurnSpeed(out, TURN_SPEED);
		putTurnSpeedS(out, TURN_SPEED);
		putTurnResistance(out, TURN_RESISTANCE);
		putTurnResistanceS(out, TURN_RESISTANCE);
		putDisplay(out);
		//putMOTDRequest(out);
		putFPSRequest(out, MAX_FPS);
		
		out.flip();
		
		sendPacket(out);
	}
	
	
	private static ReliableDataError getReliableData(ReliableData data, ByteBuffer in, ByteBuffer out)
	{
		receivePacket(in);
		in.flip();
		
		return readReliableData(data, in, out);
	}
	
	private static int readMapPacket(ByteBuffer in, ByteBuffer map, ReliableData reliable)
	{
		//readReliableData(reliable, in);
		int remaining = in.remaining();
		System.out.println("Reliable len = "+reliable.getLen()+"\nMapPacket remaining = " + remaining);
		map.put(in);
		return remaining;
	}
	
	private static int getMapPacket(ByteBuffer in, ByteBuffer map, ReliableData reliable)
	{
		ReliableDataError error = getReliableData(reliable, in, out);
		
		if (error == ReliableDataError.NO_ERROR)
			return readMapPacket(in, map, reliable);
		
		return -1;
		
		//sendAck(out, Ack.ack.setAck(reliable));
		//System.out.println(reliable);
	}
	
	private static int readFirstMapPacket(ByteBuffer in, ByteBuffer map, MapSetup setup, ReliableData reliable)
	{
		setup.readMapSetup(in);
		int remaining = in.remaining();
		System.out.println("Reliable len = "+reliable.getLen()+"\nFirstMapPacket remaining = " + remaining);
		map.put(in);
		return remaining;
	}
	
	private static int getFirstMapPacket(ByteBuffer in, ByteBuffer map, MapSetup setup, ReliableData reliable)
	{
		ReliableDataError error = getReliableData(reliable, in, out);
		
		if (error == ReliableDataError.NO_ERROR)
		{
			return readFirstMapPacket(in, map, setup, reliable);		
			//System.out.println(reliable);
		}
		return -1;
	}
	
	private static void putKeyboard(ByteBuffer buf, BitVector keyboard)
	{
		buf.put(PKT_KEYBOARD);
		buf.putInt(last_keyboard_change);
		buf.put(keyboard.getBytes());
		last_keyboard_change++;
	}
	
	private static void sendKeyboard(ByteBuffer out, BitVector keyboard)
	{
		out.clear();
		putKeyboard(out, keyboard);
		out.flip();
		sendPacket(out);
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
	private static void netSetup(ByteBuffer in, ByteBuffer map, MapSetup setup, ReliableData reliable)
	{
		int i = getFirstMapPacket(in, map, setup, reliable);
		System.out.println(setup);
		
		//map.flip();
		
		int todo = setup.getMapDataLen()-i;
		
		//map.flip();
		
		while(todo>0)
		{
			i = getMapPacket(in, map, reliable);
			if (i>=0)
				todo -= i;
		}
		
		if (setup.getMapOrder() != MapSetup.SETUP_MAP_UNCOMPRESSED)
		{
			map.flip();
			setup.uncompressMap(map);
		}
	}
	
	private static void netPacket(ByteBuffer in)
	{
		//in.clear();
		//receivePacket(in);
		//in.flip();
		
		do{
			short type = getUnsignedByte(peekByte(in));

			if (readers[type]!=null)
			{
				readers[type].readPacket(in);
			}
			else
			{
				System.out.println("Unsuported type: " + type);
				break;
			}
		}while(in.remaining()>0);

		
	}

    /**
     * Returns JXPilot's windows icon, retrieved from file and stored as
     * 'single'.
     * 
     * @return JXPilot icon.
     */
    public static Image getJXPilotIcon() {
        if (icon == null) {
            URL u = AboutWindow.class.getClassLoader().getResource(
                    "data/JXPilotIcon.png");
            icon = new ImageIcon(u).getImage();
        }

        return icon;
    }
}
