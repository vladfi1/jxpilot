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
	public static final String BURKEN_ADDRESS = "83.168.206.7";
	public static final String LOCAL_LOOPBACK_ADDRESS = "127.0.0.1";
	public static final String SERVER_IP_ADDRESS = LKRAUSS_ADDRESS;
	
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
	public static final short TURN_SPEED = 4;
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
	private static ByteBufferWrap out = new ByteBufferWrap(MAX_PACKET_SIZE);
	private static ByteBufferWrap in = new ByteBufferWrap(MAX_PACKET_SIZE);
	private static ByteBufferWrap map = new ByteBufferWrap(MAX_PACKET_SIZE);
	private static ByteBufferWrap reliableBuf = new ByteBufferWrap(MAX_PACKET_SIZE);
	private static MapSetup setup = new MapSetup();
	private static final PacketReader[] readers = new PacketReader[256];
	private static ReplyData reply = new ReplyData();
	private static ReliableData reliable = new ReliableData();
	private static ReplyMessage message = new ReplyMessage();
	private static BitVector keyboard = new BitVector(Keys.NUM_KEYS);
	private static int last_keyboard_change=0;
	
	private static MapFrame frame;
	private static Client client;
	
	/**
	 * keeps track of number of packets sent/received for debugging
	 */
	static int numPackets=0;
	
	//sets function to handle packets
	static
	{
		//have to be careful here, not all the reliable data may be in packet
		readers[PKT_RELIABLE] = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{
				in.setReading();
				System.out.println(readReliableData(reliable, in, out, reliableBuf));
				//in.clear();
			}
		};
		
		readers[PKT_REPLY] = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
			{
				if (in.remaining()<ReplyData.LENGTH)
				{
					throw new ReliableReadException();
				}
				
				System.out.println(readReplyData(in, reply));
			}
		};
		
		readers[PKT_QUIT] = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{
				byte type = in.getByte();
				try
				{
					String reason = getString(in);
					System.out.println("Server closed connection: " + reason);
					System.exit(0);
				}
				catch (StringReadException e)
				{
					e.printStackTrace();
				}
				
			}
		};
		
		readers[PKT_START] = new PacketReader()
		{
			public static final int LENGTH = 1 + 4 + 4;//9
			
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{
				byte type = in.getByte();
				int loops = in.getInt();
				int key_ack = in.getInt();
				
				
				System.out.println("\nStart Packet :" +
									"\ntype = " + type +
									"\nloops = " + loops +
									"\nkey ack = " + key_ack);
				
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
			public void readPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
			{
				int pos = in.position();
				try
				{
					byte type = in.getByte();
					short id = in.getShort();
					short myTeam = getUnsignedByte(in.getByte());
					short myChar = getUnsignedByte(in.getByte());
					String name = getString(in);
					String real = getString(in);
					String host = getString(in);

					ShipShape shape = ShipShape.parseShip(getString(in), getString(in));
					
					
					System.out.println("\nPlayer Packet\ntype = "  +type+
										"\nid = "  + id +
										"\nmy team = " + myTeam +
										"\nmy char = " + myChar +
										"\n name = " + name +
										"\nreal = " + real +
										"\nhost = " + host +
										"\nship = " + shape);
					
					client.handlePlayer(id, myTeam, myChar, name, real, host, shape);
				}
				catch(StringReadException e)
				{
					//e.printStackTrace();
					in.position(pos);
					throw new ReliableReadException();
				}
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
		
		readers[PKT_SCORE] = new PacketReader()
		{
			public static final int LENGTH = 1+2+2+2+1;
			
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{
				if (in.remaining()<LENGTH)
				{
					System.out.println("\nPacket Score ("+in.remaining()+") is too small ("+ LENGTH+")!");
					//in.clear();
					return;
				}
				
				byte type = in.getByte();
				short id = in.getShort();
				short score = in.getShort();
				short life = in.getShort();
				byte myChar = in.getByte();
				
				
				System.out.println("\nScore Packet\ntype = " + type +
									"\nid = " + id +
									"\nscore = " + score +
									"\nlife = " + life +
									"\nmy char = " + myChar);
				
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
		};
		
		readers[PKT_BASE] = new PacketReader()
		{
			public static final int LENGTH = 1 + 2 + 4;//7
			
			public void readPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
			{
				if (in.remaining()<LENGTH)
				{
					System.out.println("\nBase Packet must read " + LENGTH +", only " + in.remaining() + " left.");
					throw new ReliableReadException();
				}
				
				byte type = in.getByte();
				short id = in.getShort();
				int num = getUnsignedShort(in.getShort());
				
				System.out.println("\nBase Packet\ntype = " + type +
									"\nid = " + id +
									"\nnum = " + num);
			}
		};
		
		readers[PKT_MESSAGE] = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
			{
				int pos = in.position();
				try
				{
					byte type = in.getByte();
					String message = getString(in);
					System.out.println("\nMessage Packet\n" + message);
				}
				catch (StringReadException e)
				{
					//e.printStackTrace();
					in.position(pos);
					throw new ReliableReadException();
				}
			}
		};
		
		
		PacketReader debrisReader = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{
				//if(in.remaining()<2) return;
				
				int type = getUnsignedByte(in.getByte());
				int num = getUnsignedByte(in.getByte());
				
				
				System.out.println("\nDebris Packet" +
									"\ntype = " + type +
									"\nnum = " + num);
				in.position(in.position()+2*num);
				
			}
		};
		
		int pkt_debris = getUnsignedByte(PKT_DEBRIS);
		for (int i = 0;i<DEBRIS_TYPES;i++)
		{
			readers[i+pkt_debris] = debrisReader;
		}
		
		
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
		
		readers[PKT_SELF_ITEMS] = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client)
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
				
				System.out.println("\nSelf Items Packet\ntype = " + type +
									"\nmask = " + String.format("%x", mask));
				
			}
		};
		
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
		
		readers[PKT_SELF] = new PacketReader()
		{
			
			public static final int LENGTH = 
			(1 + 2 + 2 + 2 + 2 + 1) + (1 + 1 + 1 + 2 + 2 + 1 + 1 + 1) + (2 + 2 + 2 + 2 + 1 + 1) + 1;//31
			
			public void readPacket(ByteBufferWrap in, AbstractClient client)
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
				byte autopilotLight = in.getByte();
				
				client.handleSelf(x, y, vx, vy, 
						heading, power, turnspeed, turnresistance, 
						lockId, lockDist, lockDir, 
						nextCheckPoint, autopilotLight, 
						currentTank, fuelSum, fuelMax);
				
				System.out.println("\nPacket Self\ntype = " + type +
									"\nx = " + x +
									"\ny = " + y +
									"\nvx = " + vx +
									"\nvy = " + vy +
									"\nheading = " + heading +
									"\nautopilotLight = " + autopilotLight);
			}
		};
		
		
		readers[PKT_MODIFIERS] = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{

				int pos = in.position();
				try
				{
					byte type = in.getByte();
					String mods = getString(in);
					
					System.out.println("\nModifiers Packet\ntype = " + type +
										"\nmodifiers: " + mods);					
				}
				catch (StringReadException e)
				{
					e.printStackTrace();
					in.position(pos);
				}
			}
		};
		
		
		readers[PKT_END] = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{
				byte type = in.getByte();
				int loops = in.getInt();
				
				System.out.println("\nEnd Packet\ntype = " + type +
									"\nloops = " + loops);
			}
		};
		
		readers[PKT_BALL] = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{
				byte type = in.getByte();
				short x = in.getShort();
				short y = in.getShort();
				short id = in.getShort();
				
				System.out.println("\nBall Packet\ntype = " + type +
									"\nx = " + x +
									"\ny = " + y +
									"\nid = " + id);
			}
		};
		
		readers[PKT_SHIP] = new PacketReader()
		{
			public static final int LENGTH = 1 + 2 + 2 + 2 + 1 + 1;//9
			
			public void readPacket(ByteBufferWrap in, AbstractClient client)
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
				
				
				System.out.println("\nShip Packet\ntype = " + type +
									"\nx = " + x +
									"\ny = " + y +
									"\nid = " + id +
									"\ndir = " + dir +
									"\nshield: " + shield +
									"\ncloak: " + cloak +
									"\nemergency shield: " + emergency_shield +
									"\nphased: " + phased +
									"\ndeflector: " + deflector);
				
				client.handleShip(x, y, id, dir, shield, cloak, emergency_shield, phased, deflector);		
			}
		};
		
		readers[PKT_FASTSHOT] = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{
				byte pkt = in.getByte();
				byte type = in.getByte();
				short num = getUnsignedByte(in.getByte());
				
				if (in.remaining()<2*num)
				{
					in.clear();
					return;
				}
				
				in.position(in.position()+2*num);
				
				System.out.println("\nFastShot Packet\npkt = " + pkt +
									"\ntype = " + type +
									"\nnum = " + num);
			}
			/*
			int Receive_fastshot(void)
			{
				int			n, r, type;
			
				rbuf.ptr++;	// skip PKT_FASTSHOT packet id
			
				if (rbuf.ptr - rbuf.buf + 2 >= rbuf.len) {
					return 0;
				}
				type = (*rbuf.ptr++ & 0xFF);
				n = (*rbuf.ptr++ & 0xFF);
				if (rbuf.ptr - rbuf.buf + (n * 2) > rbuf.len) {
					return 0;
				}
				r = Handle_fastshot(type, (u_byte*)rbuf.ptr, n);
				rbuf.ptr += n * 2;
			
				return (r == -1) ? -1 : 1;
			}

			*/
			
		};
		
		readers[PKT_ITEM] = new PacketReader()
		{
			public static final int LENGTH = 1 + 2 + 2 + 1;//6
			
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{
				byte pkt = in.getByte();
				short x = in.getShort();
				short y = in.getShort();
				byte type = in.getByte();
				
				System.out.println("\nItem Packet\npkt = " + pkt +
									"\nx = " + x +
									"\ny = " + y +
									"\ntype = " + type);
				
			}
		};
		
		readers[PKT_FASTRADAR] = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{
				byte type = in.getByte();
				int n = (in.getByte() & 0xFF);
				int x, y, size;
				
				int pos = in.position();
				
				for (int i =0;i<n;i++)
				{
					x = getUnsignedByte(in.getByte());
					y= getUnsignedByte(in.getByte());
					
					byte b = in.getByte();
					
					y |= (b&0xC0) << 2;
					
					size = (b & 0x07);
					if ((b & 0x20)!=0)
					{
						size |= 0x80;
					}
					
					client.handleRadar(x, y, size);
				}
				
				System.out.println("\nFast Radar Packet:\ntype = " + type +
									"\nn = " + n +
									"\nbuffer advanced " + (in.position()-pos));
			}

		};
		
		readers[PKT_PAUSED] = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{
				byte type = in.getByte();
				short x = in.getShort();
				short y = in.getShort();
				short count = in.getShort();
				
				System.out.println("\nPaused Packet:\ntype = " +type +
									"\nx = " + x +
									"\ny = " + y +
									"\ncount = " + count);
			}
		};
		
		readers[PKT_WRECKAGE] = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{
				byte type = in.getByte();
				short x= in.getShort();
				short y = in.getShort();
				byte wrecktype = in.getByte();
				byte size = in.getByte();
				byte rot = in.getByte();
				
				System.out.println("\nWreckage Packet:\ntype = "+type +
									"\nx = " + x +
									"\ny = " + y +
									"\nwreck type = " + wrecktype +
									"\nsize = " + size +
									"\nrot = " + rot);
				
			}
		};
		
		readers[PKT_WAR] = new PacketReader()
		{
			public static final int LENGTH = 1 + 2 + 2;//5
			
			public void readPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
			{
				
				if (in.remaining()<LENGTH) throw new ReliableReadException();
				
				byte type = in.getByte();
				short robot_id = in.getShort();
				short killer_id = in.getShort();
				
				System.out.println("\nWar Packet\ntype = " + type +
									"\nRobot id = " + robot_id +
									"\nKiller id = " + killer_id);
				
			}
		};
		
		readers[PKT_CONNECTOR] = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{
				byte type = in.getByte();
				short x0 = in.getShort();
				short y0 = in.getShort();
				short x1 = in.getShort();
				short y1 = in.getShort();
				byte tractor = in.getByte();
				
				System.out.println("\nConnector Packet\ntype = " + type +
									"\nx0 = " + x0 +
									"\ny0 = " + y0 +
									"\nx1 = " + x1 +
									"\ny1 = " + y1 +
									"\ntractor = " + tractor);
			}
		};
		
		readers[PKT_LEAVE] = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{
				byte type = in.getByte();
				short id = in.getShort();
				
				System.out.println("\nLeave Packet\ntype = " + type +
									"\nid = " + id);
				
				client.handleLeave(id);
			}
		};
		
		readers[PKT_SCORE_OBJECT] = new PacketReader()
		{
			public static final int LENGTH = 1 + 2 + 2 + 2 + 1;//8
			public void readPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
			{
				if (in.remaining()<LENGTH) throw new ReliableReadException();
				
				try
				{
					byte type = in.getByte();
					//float score = (float)(in.getInt()/100.0);
					short score = in.getShort();
					int x = getUnsignedShort(in.getShort());
					int y = getUnsignedShort(in.getShort());
					String message = getString(in);
					
					System.out.println("\nScore Object Packet\ntype = " + type +
										"\nscore = " + score +
										"\nx = " + x +
										"\ny = " + y +
										"message: " + message);
				}
				catch (StringReadException e)
				{
					throw new ReliableReadException();
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
		};
		
		readers[PKT_MINE] = new PacketReader()
		{
			public void readPacket(ByteBufferWrap in, AbstractClient client)
			{
				byte type = in.getByte();
				short x = in.getShort();
				short y = in.getShort();
				byte team_mine = in.getByte();
				short id = in.getShort();
				
				System.out.println("\nMine Packet\ntype = " + type +
									"\nx = " + x +
									"\ny = " + y +
									"\nteam mine = " + team_mine +
									"\nid = " + id);
			}
		};
	}
	
	public static void main(String[] args)
	{	
		/*
        try {
            java.util.Map<String, Object> xpilotpanelEmbeddedParams = new HashMap<String, Object>();
            xpilotpanelEmbeddedParams.putByte(
                    XPilotPanel.EMBEDDED_PARAMETER_MAIN_WINDOW_TITLE,
                    "JXPilot - XPilotPanel");
            xpilotpanelEmbeddedParams.putByte(XPilotPanel.EMBEDDED_PARAMETER_ICON,
                    getJXPilotIcon());
            xpilotpanelEmbeddedParams.putByte(
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
				
				System.out.println(socket.getLocalPort());
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
				System.out.printf("Buffer : %x Actual %x\n", out.getByte(), bytes[i]);
			}
			*/
			
			
			
			//channel.send(ByteBufferWrap.wrap(first_packet), server_address);
			
			//receivePacket(in);
			
			sendJoinRequest(out, REAL_NAME, socket.getLocalPort(), NICK, HOST, TEAM);
			
			getReplyMessage(in, message);
			
			
			//getReplyMessage(in, message);
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
			
			//map.flip();
			System.out.println(map.remaining()+"\n\nMap:\n");
			
			// setup.printMapData();
	        //
	        frame = new MapFrame(new Map(setup));
	        client = new Client(frame);
	        frame.setVisible(true);
			
	        System.out.println("\nSending Net Start");
			netStart(out);
			
			/*
			while(getReliableData(reliable, in, out)!=ReliableDataError.NO_ERROR)
			System.out.println(reliable);
			
			//in.position(in.position()-ReliableData.LENGTH);
			
			System.out.println(readReplyData(in, reply));
			
			in.position(in.position()-(ReliableData.LENGTH + ReplyData.LENGTH));
			
			netPacket(in);
			*/
			
			//System.out.println(keyboard.setBits());
			//System.out.println(keyboard.setBit(Keys.KEY_SELF_DESTRUCT, false));
			//System.out.println(keyboard.setBit(Keys.KEY_PAUSE, false));
			keyboard.clearBits();
			keyboard.setBit(Keys.KEY_TURN_RIGHT, true);
			
			System.out.println("\nStarting input loop.");
            while(true)
			{
            	in.clear();
            	in.receivePacket(channel);
        		//in.flip();
        		
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
	
	public static byte peekByte(ByteBufferWrap buf)
	{
		//return buf.get(0);
		
		byte b = buf.getByte();
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
	
	
	private static StringBuffer strBuf = new StringBuffer();
	
	//uses 1 byte chars
	public static String getString(ByteBufferWrap buffer) throws StringReadException
	{
		StringBuffer b = new StringBuffer();
		byte ch;
		//boolean ends_with_null = false;
		
		do
		{
			
			if (buffer.remaining()<=0)
			{
				/*
				System.out.println("*****error reading string!******");
				String soFar = "";
				
				for (byte c : b.toString().getBytes())
				{
					soFar += String.format("%x ", c);
				}
				System.out.println("So far getString() has read:\n" +soFar);
				*/
				throw new StringReadException();
			}
			
			
			ch = buffer.getByte();
			
			//if (ch==END_OF_STRING)
			//	ends_with_null = true;
			
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
	
	public static void putJoinRequest(ByteBufferWrap buf, String real_name, int port, String nick, String host, int team)
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
	
	public static void sendJoinRequest(ByteBufferWrap buf, String real_name, int port, String nick, String host, int team)
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
	
	public static void putJoinRequest(ByteBuffer buf, String real_name, int port, String nick, String host, int team)
	{
		buf.clear();
		
		buf.putInt(MAGIC);
		putString(buf, real_name);
		//buf.putByte(PKT_MESSAGE);
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
			//buf.flip();
			channel.send(buf, server_address);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public static void receivePacket(ByteBufferWrap buf)
	{
		buf.clear();
		try
		{
			buf.receivePacket(channel);
			numPackets++;
			System.out.println("\nGot Packet-number: " + numPackets + ", " + buf.position() + " bytes.");
			//System.out.println("\nBuf received " + buf.position());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static void sendPacket(ByteBufferWrap buf)
	{
		try
		{
			buf.sendPacket(channel, server_address);	
			numPackets++;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void putVerify(ByteBufferWrap buf, String real_name, String nick)
	{
		//buf.clear();
		
		buf.putByte(PKT_VERIFY);
		buf.putString(real_name);
		buf.putString(nick);
		buf.putString(DISPLAY);
	}
	
	private static void sendVerify(ByteBufferWrap buf, String real_name, String nick)
	{
		buf.clear();
		putVerify(buf, real_name, nick);
		try
		{
			//buf.flip();
			buf.sendPacket(channel, server_address);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void sendAck(ByteBufferWrap buf, Ack ack)
	{
		buf.clear();
		putAck(buf, ack);
		try
		{
			//buf.flip();
			buf.sendPacket(channel, server_address);
			
			System.out.println("\n"+ack);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void putPower(ByteBufferWrap buf, short power)
	{
		buf.putByte(PKT_POWER);
		buf.putShort((short)(power*256));
	}
	private static void putPowerS(ByteBufferWrap buf, short power)
	{
		buf.putByte(PKT_POWER_S);
		buf.putShort((short)(power*256));
	}
	
	private static void putTurnSpeed(ByteBufferWrap buf, short turn_speed)
	{
		buf.putByte(PKT_TURNSPEED);
		buf.putShort((short)(turn_speed*256));
	}
	private static void putTurnSpeedS(ByteBufferWrap buf, short turn_speed)
	{
		buf.putByte(PKT_TURNSPEED_S);
		buf.putShort((short)(turn_speed*256));
	}
	private static void putTurnResistance(ByteBufferWrap buf, short turn_resistance)
	{
		buf.putByte(PKT_TURNRESISTANCE);
		buf.putShort((short)(turn_resistance*256));
	}
	private static void putTurnResistanceS(ByteBufferWrap buf, short turn_resistance)
	{
		buf.putByte(PKT_TURNRESISTANCE_S);
		buf.putShort((short)(turn_resistance*256));
	}
	private static void putDisplay(ByteBufferWrap buf)
	{
		buf.putByte(PKT_DISPLAY);
		buf.putShort(WIDTH_WANTED);
		buf.putShort(HEIGHT_WANTED);
		buf.putByte(NUM_SPARK_COLORS);
		buf.putByte(SPARK_RAND);
	}
	private static void putMOTDRequest(ByteBufferWrap buf)
	{
		buf.putByte(PKT_MOTD);
		buf.putBytes(MOTD_BYTES);
	}
	
	private static void putFPSRequest(ByteBufferWrap buf, byte max_fps)
	{
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
	private static void netStart(ByteBufferWrap out)
	{
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
	
	
	private static ReliableDataError getReliableData(ReliableData data, ByteBufferWrap in, ByteBufferWrap out)
	{
		receivePacket(in);
		//in.flip();
		
		return readReliableData(data, in, out);
	}
	
	private static int readMapPacket(ByteBufferWrap in, ByteBufferWrap map, ReliableData reliable)
	{
		//readReliableData(reliable, in);
		int remaining = in.remaining();
		System.out.println("Reliable len = "+reliable.getLen()+"\nMapPacket remaining = " + remaining);
		map.putBytes(in);
		return remaining;
	}
	
	private static int getMapPacket(ByteBufferWrap in, ByteBufferWrap map, ReliableData reliable)
	{
		ReliableDataError error = getReliableData(reliable, in, out);
		
		if (error == ReliableDataError.NO_ERROR)
			return readMapPacket(in, map, reliable);
		
		return -1;
		
		//sendAck(out, Ack.ack.setAck(reliable));
		//System.out.println(reliable);
	}
	
	private static int readFirstMapPacket(ByteBufferWrap in, ByteBufferWrap map, MapSetup setup, ReliableData reliable)
	{
		setup.readMapSetup(in);
		int remaining = in.remaining();
		System.out.println("Reliable len = "+reliable.getLen()+"\nFirstMapPacket remaining = " + remaining);
		map.putBytes(in);
		return remaining;
	}
	
	private static int getFirstMapPacket(ByteBufferWrap in, ByteBufferWrap map, MapSetup setup, ReliableData reliable)
	{
		
		while(getReliableData(reliable, in, out)!=ReliableDataError.NO_ERROR)
		{
			System.out.println("Didn't get reliable data when waiting for map");
		}
		
		return readFirstMapPacket(in, map, setup, reliable);		
	}
	
	private static void putKeyboard(ByteBufferWrap buf, BitVector keyboard)
	{
		buf.putByte(PKT_KEYBOARD);
		buf.putInt(last_keyboard_change);
		buf.putBytes(keyboard.getBytes());
		last_keyboard_change++;
	}
	
	private static void sendKeyboard(ByteBufferWrap out, BitVector keyboard)
	{
		out.clear();
		putKeyboard(out, keyboard);
		//out.flip();
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
	private static void netSetup(ByteBufferWrap in, ByteBufferWrap map, MapSetup setup, ReliableData reliable)
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
			//map.flip();
			setup.uncompressMap(map);
		}
	}
	
	private static void netPacket(ByteBufferWrap in)
	{
		//in.clear();
		//receivePacket(in);
		//in.flip();
		
		while(in.remaining()>0)
		{
			short type = getUnsignedByte(peekByte(in));
			
			if (readers[type]!=null)
			{	
				try
				{
					readers[type].readPacket(in, client);
				}
				catch(PacketReadException e)
				{
					in.clear();
					e.printStackTrace();
				}
			}
			else
			{
				System.out.println("**********Unsuported type: " + type + "************");
				break;
			}
		}

		
		

		//System.out.println("\nBefore reading from reliableBuf, reliableBuf_writing = " + reliableBuf_writing);
		
		reliableBuf.setReading();
		
		if (reliableBuf.remaining()>0)
		{
			while (reliableBuf.remaining()>0)
			{
				System.out.println("\nAttempting to read from reliableBuf");
				
				short type = reliableBuf.peekByte();

				if(readers[type]!=null)
				{
					int pos = reliableBuf.position();
					try
					{
						readers[type].readPacket(reliableBuf, client);
					}
					catch (ReliableReadException e)
					{
						System.out.println("Fragmented reliable packet of type: " + type);
						reliableBuf.position(pos);
						break;
					}
					catch(PacketReadException e)
					{
						e.printStackTrace();
						break;
					}
				}
				else
				{
					System.out.println("**********Unsuported Reliable type: " + type + "************");
					break;
				}	
			}

			//reliableBuf.compact();
			
			if (reliableBuf.remaining()<=0)
			{
				reliableBuf.clear();
				System.out.println("Clearing reliable buffer");
			}
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
