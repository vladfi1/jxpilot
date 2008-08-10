package net.sf.jxpilot.test;

import static net.sf.jxpilot.test.ReliableDataError.*;
import static net.sf.jxpilot.test.ReliableData.*;
import java.net.*;
import java.io.*;
import java.nio.channels.*;
import java.nio.*;
import java.nio.charset.*;

public class UDPTest {
	public static final int MAX_PACKET_SIZE = 65507;
	public static final int MAGIC =0x4501F4ED;
	public static final short SERVER_MAIN_PORT = 15345;
	public static final short CLIENT_PORT = 15345;
	
	public static final String LKRAUSS_ADDRESS = "213.239.204.35";
	public static final String STROWGER_ADDRESS = "149.156.203.245";
	public static final String CHAOS_ADDRESS = "129.13.108.207";
	public static final String SERVER_IP_ADDRESS = LKRAUSS_ADDRESS;
	
	public static final byte END_OF_STRING = 0x00;
	
	public static final byte ENTER_QUEUE_pack = 0x01;
	public static final byte ENTER_GAME_pack = 0x00;
	public static final byte SUCCESS = 0x00;
	public static final byte PKT_VERIFY = 0x01;
	public static final byte PKT_ACK = 0x2B;
	
	public static final int TEAM = 0x0000FFFF;
	public static final String DISPLAY = "";
	public static final String REAL_NAME = "vlad";
	public static final String NICK = "Vlad";
	public static final String HOST = "xxx";
	
	static InetSocketAddress server_address;
	static DatagramSocket socket;
	static DatagramChannel channel;
	static ByteBuffer out = ByteBuffer.allocate(MAX_PACKET_SIZE);
	static ByteBuffer in = ByteBuffer.allocate(MAX_PACKET_SIZE);
	static ByteBuffer map = ByteBuffer.allocate(MAX_PACKET_SIZE);
	static MapSetup setup = new MapSetup();
	
	public static void main(String[] args)
	{	
		try
		{
			
			try{
				server_address = new InetSocketAddress(SERVER_IP_ADDRESS, SERVER_MAIN_PORT);
				
				channel = DatagramChannel.open();
				socket = channel.socket();
				socket.bind(new InetSocketAddress(CLIENT_PORT));
				
				
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
			
			
			ReplyData reply = new ReplyData();
			ReliableData reliable = new ReliableData();
			
			channel.send(ByteBuffer.wrap(first_packet), server_address);
			
			receivePacket(in);
			
			sendJoinRequest(out, REAL_NAME, CLIENT_PORT, NICK, HOST, TEAM);
			
			getReplyData(reply, in);
			System.out.println(reply);
			
			while(reply.getPack()!=ENTER_GAME_pack)
			{
				getReplyData(reply, in);
				System.out.println(reply);
			}
			
			int server_port = reply.getValue();
			System.out.println("New server port: "+server_port);
			
			server_address = new InetSocketAddress(SERVER_IP_ADDRESS, server_port);
			
			System.out.println("Sending Verify");
			sendVerify(out, REAL_NAME, NICK);
			
			ReliableDataError result=null;
			while (result!=NO_ERROR)
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
				if (error==NO_ERROR)
					todo -= reliable.getLen();
			}
			
			
			/*
			map.flip();
			for (int i = 0;i<map.remaining();i++)
			{
				System.out.printf("%x\n", map.get());
			}
			*/
			
			
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
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//uses 1 byte chars
	public static void putString(ByteBuffer buffer, String str)
	{
		try
		{
			Charset c = null;
			buffer.put(str.getBytes("US-ASCII"));
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
	
	public static void putJoinRequest(ByteBuffer buf, String real_name, short port, String nick, String host, int team)
	{
		buf.clear();
		
		buf.putInt(MAGIC);
		putString(buf, real_name);
		buf.putShort(port);
		
		
		buf.put(ENTER_QUEUE_pack);
		putString(buf, nick);
		putString(buf, DISPLAY);
		putString(buf, host);
		buf.putInt(team);
	}
	
	public static void sendJoinRequest(ByteBuffer buf, String real_name, short port, String nick, String host, int team)
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
	
	public static ReplyData readReplyData(ReplyData data, ByteBuffer buf)
	{
		//buf.rewind();
		
		data.setData(buf.getInt(), buf.get(), buf.get(),  buf.remaining()>0 ? buf.getShort() : (short)0);
		return data;
	}
	
	public static ReplyData getReplyData(ReplyData data, ByteBuffer buf)
	{
		receivePacket(buf);
		buf.flip();
		return readReplyData(data, buf);
	}
	
	public static void putVerify(ByteBuffer buf, String real_name, String nick)
	{
		buf.clear();
		
		buf.put(PKT_VERIFY);
		putString(buf, real_name);
		putString(buf, nick);
		putString(buf, DISPLAY);
	}
	
	public static void sendVerify(ByteBuffer buf, String real_name, String nick)
	{
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
		buf.clear();
		
		buf.put(ack.getType());
		buf.putInt(ack.getOffset());
		buf.putInt(ack.getLoops());
	}
	
	public static void sendAck(ByteBuffer buf, Ack ack)
	{
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
	
	
	
	private static void readMapPacket(ByteBuffer in, ByteBuffer map, ReliableData reliable)
	{
		//readReliableData(reliable, in);
		map.put(in);
	}
	
	private static ReliableDataError getMapPacket(ByteBuffer in, ByteBuffer map, ReliableData reliable)
	{
		ReliableDataError error = getReliableData(reliable, in, out);
		
		if (error == NO_ERROR)
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
		
		if (error == NO_ERROR)
		{
			readFirstMapPacket(in, map, setup);		
			//System.out.println(reliable);
		}
	}
	
	
}

class ReplyData
{
	private int magic;
	private byte pack_type,
				status;
	private int value;
	
	public ReplyData setData(int magic, byte pack_type, byte status, short value)
	{
		this.magic = magic;
		this.pack_type = pack_type;
		this.status = status;
		this.value = getUnsignedShort(value);
		return this;
	}
	
	public int getMagic(){return magic;}
	public byte getPack(){return pack_type;}
	public byte getStatus(){return status;}
	public int getValue(){return value;}
	
	public String toString()
	{
		return "Reply Data:\n"
				+"magic = " + String.format("%x", magic)
				+"\npack_type = " + String.format("%x", pack_type)
				+"\nstatus = " + String.format("%x", status)
				+"\nvalue = " + String.format("%x", value);
	}
	
	public static int getUnsignedShort(short val)
	{
		return (int)((char)val);
	}
}

class Ack
{
	public static Ack ack = new Ack();
	
	private byte type = UDPTest.PKT_ACK;
	private int reliable_offset;
	private int reliable_loops;
	
	public Ack setAck(int offset, int loops)
	{
		reliable_offset = offset;
		reliable_loops = loops;
		return this;
	}
	
	public Ack setAck(ReliableData data)
	{
		return setAck(data.getOffset(), data.getRelLoops());
	}
	
	public byte getType(){return type;}
	public int getOffset(){return reliable_offset;}
	public int getLoops(){return reliable_loops;}
	
	public String toString()
	{
		return  "Acknowledgement"
				+ "\ntype = " + String.format("%x", type)
				+ "\nreliable offset = " + reliable_offset
				+ "\nreliable loops = " + reliable_loops;
	}
}

class MapSetup
{
	/*
	n = Packet_scanf(&cbuf,
			"%ld" "%ld%hd" "%hd%hd" "%hd%hd" "%s%s",
			&Setup->map_data_len,
			&Setup->mode, &Setup->lives,
			&Setup->x, &Setup->y,
			&Setup->frames_per_second, &Setup->map_order,
			Setup->name, Setup->author);
	*/
	
	//ints
	private int map_data_len, mode;
	private short lives, x, y, frames_per_second, map_order;
	private String name, author;
	
	public MapSetup setMapSetup(int map_data_len, int mode, short lives, short x, short y, 
								short frames_per_second, short map_order, String name, String author)
	{
		this.map_data_len = map_data_len;
		this.mode = mode;
		this.lives = lives;
		this.x=x;
		this.y=y;
		this.frames_per_second = frames_per_second;
		this.map_order = map_order;
		this.name = name;
		this.author = author;
		return this;
	}
	
	public int getMapDataLen(){return map_data_len;}
	public int getMode(){return mode;}
	public short getLives(){return lives;}
	public short getX(){return x;}
	public short getY(){return y;}
	public short getFramesPerSecond(){return frames_per_second;}
	public short getMapOrder(){return map_order;}
	public String getName(){return name;}
	public String getAuthor(){return author;}
	
	public String toString()
	{
		return "Map Setup"
				+ "\nmap Data length = " + map_data_len
				+ "\nmode = " + mode
				+ "\nlives = " + lives
				+ "\nx = " + x
				+ "\ny = " + y
				+ "\nframes per second = " + frames_per_second
				+ "\nmap order = " + map_order
				+ "\nname = " + name
				+ "\nauthor = " + author;
	}
	
}