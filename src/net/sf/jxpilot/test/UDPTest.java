package net.sf.jxpilot.test;

import static net.sf.jxpilot.test.Packet.*;
import static net.sf.jxpilot.test.ReplyData.*;
import javax.swing.*;

import net.sf.xpilotpanel.XPilotPanel;
import net.sf.xpilotpanel.gui.AboutWindow;
import static net.sf.jxpilot.test.MapError.*;
import static net.sf.jxpilot.test.ReliableDataError.*;
import static net.sf.jxpilot.test.ReliableData.*;
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
	public static final String HOST = "xxx";
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

    // client objects
	static InetSocketAddress server_address;
	static DatagramSocket socket;
	static DatagramChannel channel;
	static ByteBuffer out = ByteBuffer.allocate(MAX_PACKET_SIZE);
	static ByteBuffer in = ByteBuffer.allocate(MAX_PACKET_SIZE);
	static ByteBuffer map = ByteBuffer.allocate(MAX_PACKET_SIZE);
	static MapSetup setup = new MapSetup();
	static PacketReader[] readers = new PacketReader[256];
	static ReplyData reply = new ReplyData();
	static ReliableData reliable = new ReliableData();
	static ReplyMessage message = new ReplyMessage();
	
	static
	{
		readers[PKT_RELIABLE] = new PacketReader()
		{
			public void readPacket(ByteBuffer buf)
			{
				System.out.println(readReliableData(reliable, buf));
				buf.clear();
			}
		};
	}
	
	public static void main(String[] args)
	{	

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

        // runClient(SERVER_IP_ADDRESS, SERVER_MAIN_PORT);

        // setup.printMapData();
        //
        // MapFrame frame = new MapFrame(new Map(setup));
        //
        // frame.setVisible(true);
			
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
			
			
			getFirstMapPacket(in, map, setup, reliable);
			System.out.println(setup);
			
			int todo = setup.getMapDataLen()-reliable.getLen();
			
			while(todo>0)
			{
				ReliableDataError error = getMapPacket(in, map, reliable);
				if (error==ReliableDataError.NO_ERROR)
					todo -= reliable.getLen();
			}
			
			
			if (setup.getMapOrder() != MapSetup.SETUP_MAP_UNCOMPRESSED)
			{
				setup.uncompressMap(map);
			}
			
			map.flip();
			System.out.println(map.remaining()+"\n\nMap:\n");
			
			netStart(out);
			
			while(getReliableData(reliable, in, out)!=ReliableDataError.NO_ERROR)
			System.out.println(reliable);
			
			System.out.println(readReplyData(in, reply));
			in.clear();

            while(true)
			{
				netPacket();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
					
	}
	
	public static byte peekByte(ByteBuffer buf)
	{
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
		return (short)(0xFF & (int)val);
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
			System.out.println("\nBuf received " + buf.position());
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

	private static ReplyMessage readReplyMessage(ByteBuffer buf, ReplyMessage message)
	{
		message.setMessage(buf.getInt(), buf.get(), buf.get(), buf.remaining()>=2 ? buf.getShort(): 0);
		return message;
	}
	private static ReplyMessage getReplyMessage(ByteBuffer buf, ReplyMessage message)
	{
		receivePacket(buf);
		buf.flip();
		return readReplyMessage(buf, message);
	}

	public static void putVerify(ByteBuffer buf, String real_name, String nick)
	{
		//buf.clear();
		
		buf.put(PKT_VERIFY);
		putString(buf, real_name);
		putString(buf, nick);
		putString(buf, DISPLAY);
	}
	
	public static void sendVerify(ByteBuffer buf, String real_name, String nick)
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
	
	public static void putAck(ByteBuffer buf, Ack ack)
	{
		//buf.clear();
	
		buf.put(ack.getType());
		buf.putInt(ack.getOffset());
		buf.putInt(ack.getLoops());
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
	
	private static void readMapPacket(ByteBuffer in, ByteBuffer map, ReliableData reliable)
	{
		//readReliableData(reliable, in);
		map.put(in);
	}
	
	private static ReliableDataError getMapPacket(ByteBuffer in, ByteBuffer map, ReliableData reliable)
	{
		ReliableDataError error = getReliableData(reliable, in, out);
		
		if (error == ReliableDataError.NO_ERROR)
			readMapPacket(in, map, reliable);
		
		return error;
		
		//sendAck(out, Ack.ack.setAck(reliable));
		//System.out.println(reliable);
	}
	
	private static MapSetup readMapSetup(ByteBuffer in, MapSetup setup)
	{	
		return setup.setMapSetup(in.getInt(), in.getInt(), in.getShort(), in.getShort(), in.getShort(),
				in.getShort(), in.getShort(), getString(in), getString(in));
	}
	
	private static void readFirstMapPacket(ByteBuffer in, ByteBuffer map, MapSetup setup)
	{
		readMapSetup(in, setup);
		map.put(in);
	}
	
	private static void getFirstMapPacket(ByteBuffer in, ByteBuffer map, MapSetup setup, ReliableData reliable)
	{
		ReliableDataError error = getReliableData(reliable, in, out);
		
		if (error == ReliableDataError.NO_ERROR)
		{
			readFirstMapPacket(in, map, setup);		
			//System.out.println(reliable);
		}
	}
	
	private static void netPacket()
	{
		in.clear();
		receivePacket(in);
		
		short type = getUnsignedByte(peekByte(in));
		
		if (readers[type]!=null)
		{
			readers[type].readPacket(in);
		}
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
