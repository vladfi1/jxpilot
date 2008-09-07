package net.sf.jxpilot.test;

import static net.sf.jxpilot.test.UDPTest.PRINT_PACKETS;
import static net.sf.jxpilot.test.Ack.putAck;
import static net.sf.jxpilot.test.Packet.*;
import static net.sf.jxpilot.test.ReplyData.readReplyData;
import static net.sf.jxpilot.test.Utilities.removeNullCharacter;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.*;

import net.sf.xpilotpanel.preferences.Preferences;

public class NetClient
{
	private static final Random rnd = new Random();
	
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
	public static final short TURN_SPEED = 10;
	public static final short TURN_RESISTANCE = 0;
	public static final byte MAX_FPS = (byte)20;
	public static final byte[] MOTD_BYTES = {0,0,0x47,2,0,0x43,3};

	//used for sending display
	public static final short WIDTH_WANTED = 0x400;
	public static final short HEIGHT_WANTED = 0x400;
	public static final byte NUM_SPARK_COLORS = 0x08;
	public static final byte SPARK_RAND = 0x33;

	private static final ShipShape SHIP = ShipShape.defaultShip;

	// Nick, user and host to send to server. This default values are for if
    // JXPilot was lauched w/o XPilotPanel.
    private String NICK = null;
    private String REAL_NAME = null;
    private String HOST = null;

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
	
	/**
	 * Keeps track last frame update number.
	 * This is also used for sending acknowledgements.
	 */
	private int last_loops;
	private volatile boolean quit = false;
	
	/**
	 * keeps track of how far pointer has moved since the last frame update.
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
		
		//sets function to handle packets
		
		//have to be careful here, not all the reliable data may be in packet
		readers[PKT_RELIABLE] = new PacketProcessor()
		{
			public void processPacket(ByteBufferWrap in, AbstractClient client)
			{
				in.setReading();
				if(PRINT_PACKETS)
				{
					System.out.println(reliable.readReliableData(in, NetClient.this, reliableBuf));
				}
				else
				{
					reliable.readReliableData(in, NetClient.this, reliableBuf);
				}
				//in.clear();
			}
		};

		readers[PKT_REPLY] = new PacketProcessor()
		{
			public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
			{
				if (in.remaining()<ReplyData.LENGTH)
				{
					throw reliableReadException;
				}

				if(PRINT_PACKETS)
				{
					System.out.println(readReplyData(in, reply));
				}
				else
				{
					readReplyData(in, reply);
				}
			}
		};

		readers[PKT_QUIT] = new PacketProcessor()
		{
			public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
			{
				byte type = in.getByte();
				try
				{
					String reason = in.getString();
					System.out.println("Server closed connection: " + reason);
					System.exit(0);
				}
				catch (StringReadException e)
				{
					//e.printStackTrace();
					throw reliableReadException;
				}

			}
		};

		readers[PKT_START] = new PacketProcessor()
		{
			public static final int LENGTH = 1 + 4 + 4;//9

			public void processPacket(ByteBufferWrap in, AbstractClient client)
			{
				
				byte type = in.getByte();
				int loops = in.getInt();
				int key_ack = in.getInt();

				numFrames++;
				
				//packet is duplicate or out of order
				if (last_loops >= loops)
				{
					in.clear();
					return;
				}
				
				last_loops = loops;
				
				
				if(PRINT_PACKETS)System.out.println("\nStart Packet :" +
						"\ntype = " + type +
						"\nloops = " + loops +
						"\nkey ack = " + key_ack);
				client.handleStart(loops);
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

		readers[PKT_PLAYER] = new PacketProcessor()
		{
			public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
			{
				int pos = in.position();
				try
				{
					byte type = in.getByte();
					short id = in.getShort();
					short myTeam = in.getUnsignedByte();
					short myChar = in.getUnsignedByte();
					String name = removeNullCharacter(in.getString());
                    String real = removeNullCharacter(in.getString());
                    String host = removeNullCharacter(in.getString());

					ShipShape shape = ShipShape.parseShip(in.getString(), in
                            .getString());

					if(PRINT_PACKETS)System.out.println("\nPlayer Packet\ntype = "  +type+
							"\nid = "  + id +
							"\nmy team = " + myTeam +
							"\nmy char = " + myChar +
							"\n name = " + name +
							"\nreal = " + real +
							"\nhost = " + host +
							"\nship = " + shape);

					client.handlePlayer(new Player(id, myTeam, myChar, name, real, host, shape));
				}
				catch(StringReadException e)
				{
					//e.printStackTrace();
					in.position(pos);
					throw reliableReadException;
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

		readers[PKT_SCORE] = new PacketProcessor()
		{
			public static final int LENGTH = 1+2+2+2+1;

			public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
			{
				if (in.remaining()<LENGTH)
				{
					//if(PRINT_PACKETS)System.out.println("\nPacket Score ("+in.remaining()+") is too small ("+ LENGTH+")!");

					//in.clear();
					throw reliableReadException;
				}

				byte type = in.getByte();
				short id = in.getShort();
				short score = in.getShort();
				short life = in.getShort();
				byte myChar = in.getByte();


				if(PRINT_PACKETS)System.out.println("\nScore Packet\ntype = " + type +
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

		readers[PKT_BASE] = new PacketProcessor()
		{
			public static final int LENGTH = 1 + 2 + 4;//7

			private BaseHolder b = new BaseHolder();
			
			public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
			{
				if (in.remaining()<LENGTH)
				{
					if(PRINT_PACKETS)
						System.out.println("\nBase Packet must read " + LENGTH +", only " + in.remaining() + " left.");
					throw reliableReadException;
				}

				byte type = in.getByte();
				short id = in.getShort();
				int num = in.getUnsignedShort();

				if(PRINT_PACKETS)
					System.out.println("\nBase Packet\ntype = " + type +
						"\nid = " + id +
						"\nnum = " + num);
				
				client.handleBase(b.setBase(id, num));
			}
		};

		readers[PKT_MESSAGE] = new PacketProcessor()
		{
			public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
			{
				int pos = in.position();

                // Message to print in client.
                String message = null;

                try {
                    byte type = in.getByte();
                    message = in.getString();
                    if (PRINT_PACKETS)
                        System.out.println("\nMessage Packet\n" + message);
                }
                catch (StringReadException e) {
                    // e.printStackTrace();
                    in.position(pos);
                    throw reliableReadException;
                }

                if (message != null) {
                    message = removeNullCharacter(message);
                    client.handleMessage(message);
                }
			}
		};


		PacketProcessor debrisReader = new PacketProcessor()
		{
			private AbstractDebrisHolder d = new AbstractDebrisHolder();
			
			public void processPacket(ByteBufferWrap in, AbstractClient client)
			{
				//if(in.remaining()<2) return;

				int type = in.getUnsignedByte();
				int num = in.getUnsignedByte();


				if(PRINT_PACKETS)
					System.out.println("\nDebris Packet" +
						"\ntype = " + type +
						"\nnum = " + num);
				
				for (int i = 0;i<num;i++)
				{
					client.handleDebris(d.setAbstractDebris(type-DEBRIS_TYPES, in.getUnsignedByte(), in.getUnsignedByte()));
				}
				
				//in.position(in.position()+2*num);

			}
		};

		int pkt_debris = Utilities.getUnsignedByte(PKT_DEBRIS);

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

		readers[PKT_SELF_ITEMS] = new PacketProcessor()
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

				if(PRINT_PACKETS)System.out.println("\nSelf Items Packet\ntype = " + type +
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

		readers[PKT_SELF] = new PacketProcessor()
		{

			public static final int LENGTH = 
				(1 + 2 + 2 + 2 + 2 + 1) + (1 + 1 + 1 + 2 + 2 + 1 + 1 + 1) + (2 + 2 + 2 + 2 + 1 + 1) + 1;//31

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

				client.handleSelf(x, y, vx, vy, 
						heading, power, turnspeed, turnresistance, 
						lockId, lockDist, lockDir, nextCheckPoint, 
						currentTank, fuelSum, fuelMax,
						ext_view_width, ext_view_height,
						debris_colors, stat, autopilot_light);

				if(PRINT_PACKETS)
					System.out.println("\nPacket Self\ntype = " + type +
						"\nx = " + x +
						"\ny = " + y +
						"\nvx = " + vx +
						"\nvy = " + vy +
						"\nheading = " + heading +
						"\nautopilotLight = " + autopilot_light);
			}
		};


		readers[PKT_MODIFIERS] = new PacketProcessor()
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
		};


		readers[PKT_END] = new PacketProcessor()
		{
			public void processPacket(ByteBufferWrap in, AbstractClient client)
			{
				byte type = in.getByte();
				int loops = in.getInt();
				
				if(PRINT_PACKETS)System.out.println("\nEnd Packet\ntype = " + type +
						"\nloops = " + loops);
				
				client.handleEnd(loops);
			}
		};

		readers[PKT_BALL] = new PacketProcessor()
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

		readers[PKT_SHIP] = new PacketProcessor()
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

		readers[PKT_FASTSHOT] = new PacketProcessor()
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
		};

		readers[PKT_ITEM] = new PacketProcessor()
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
		};

		readers[PKT_FASTRADAR] = new PacketProcessor()
		{
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

					client.handleRadar(x, y, size);
				}

				if(PRINT_PACKETS)System.out.println("\nFast Radar Packet:\ntype = " + type +
						"\nn = " + n +
						"\nbuffer advanced " + (in.position()-pos));
			}

		};

		readers[PKT_PAUSED] = new PacketProcessor()
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
		};

		readers[PKT_WRECKAGE] = new PacketProcessor()
		{
			public void processPacket(ByteBufferWrap in, AbstractClient client)
			{
				byte type = in.getByte();
				short x= in.getShort();
				short y = in.getShort();
				byte wrecktype = in.getByte();
				byte size = in.getByte();
				byte rot = in.getByte();

				if(PRINT_PACKETS)System.out.println("\nWreckage Packet:\ntype = "+type +
						"\nx = " + x +
						"\ny = " + y +
						"\nwreck type = " + wrecktype +
						"\nsize = " + size +
						"\nrot = " + rot);

			}
		};

		readers[PKT_WAR] = new PacketProcessor()
		{
			public static final int LENGTH = 1 + 2 + 2;//5

			public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
			{

				if (in.remaining()<LENGTH) throw reliableReadException;

				byte type = in.getByte();
				short robot_id = in.getShort();
				short killer_id = in.getShort();

				if(PRINT_PACKETS)System.out.println("\nWar Packet\ntype = " + type +
						"\nRobot id = " + robot_id +
						"\nKiller id = " + killer_id);

			}
		};

		readers[PKT_CONNECTOR] = new PacketProcessor()
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
		};

		readers[PKT_LEAVE] = new PacketProcessor()
		{
			public static final int LENGTH = 1 + 2;//3

			public void processPacket(ByteBufferWrap in, AbstractClient client) throws ReliableReadException
			{
				if (in.remaining()<LENGTH) throw reliableReadException;

				byte type = in.getByte();
				short id = in.getShort();

				if(PRINT_PACKETS)System.out.println("\nLeave Packet\ntype = " + type +
						"\nid = " + id);

				client.handleLeave(id);
			}
		};

		readers[PKT_SCORE_OBJECT] = new PacketProcessor()
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
		};

		readers[PKT_MINE] = new PacketProcessor()
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
		};

		readers[PKT_CANNON] = new PacketProcessor()
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
		};
		
		readers[PKT_FUEL] = new PacketProcessor()
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
		};
		
		readers[PKT_MISSILE] = new PacketProcessor()
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
		};
		
		readers[PKT_DAMAGED] = new PacketProcessor()
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
		};
		
		readers[PKT_LASER] = new PacketProcessor()
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
		};
		
		readers[PKT_ECM] = new PacketProcessor()
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
		};
		
		readers[PKT_REFUEL] = new PacketProcessor()
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
		};
		
		readers[PKT_TALK_ACK] = new PacketProcessor()
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
		};
	}

	public void runClient(String serverIP, int serverPort)
	{
		try{
			server_address = new InetSocketAddress(serverIP, serverPort);

			channel = DatagramChannel.open();
			socket = channel.socket();
			//socket.connect(server_address);
			channel.connect(server_address);
			
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
		while (result!=ReliableDataError.NO_ERROR)
		{
			result = getReliableData(reliable, in);
			//System.out.println(result);
			
			if (result != ReliableDataError.BAD_PACKET && result != ReliableDataError.NOT_RELIABLE_DATA)
			{
				sendPacket(out);
			}
		}
		
		netSetup(in, map, setup, reliable);

		//map.flip();
		System.out.println(map.remaining()+"\n\nMap:\n");

		// setup.printMapData();
		//

		client.mapInit(new BlockMap(setup));

		System.out.println("\nSending Net Start");
		netStart(out);

		keyboard.clearBits();
		//keyboard.setBit(Keys.KEY_TURN_RIGHT, true);

		System.out.println("\nStarting input loop.");
		startTime = System.currentTimeMillis();
		
		inputLoop();
		
		System.out.println("\nEnd of input loop");
	}
	
	private void inputLoop()
	{	
		try
		{
			channel.configureBlocking(true);
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		//long lastFrameTime = 0;
		//long min_interval = 1000000000/MAX_FPS;
		//long currentFrameTime;
		
		while(!quit)
		{
			
			//readPacket(in);
			readLatestPacket(in);
			
			netPacket(in, reliableBuf);
			
			client.handlePacketEnd();
			
			//this.netTalk("Hello");
			
			putPointerMove(out);
			putKeyboard(out, keyboard);
			sendTalk(out);
			sendPacket(out);
			out.clear();
		}
		
		sendQuit();
	}
	
	private ByteBufferWrap temp = new ByteBufferWrap(MAX_PACKET_SIZE);
	/**
	 * Reads the last packet sent into in.
	 * @param in The ByteBufferWrap in which the packet is read.
	 */
	private void readLatestPacket(ByteBufferWrap in)
	{
		try
		{
			channel.configureBlocking(true);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		readPacket(in);
		
		try
		{
			channel.configureBlocking(false);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		do
		{
			temp.clear();
			temp.putBytes(in);
		}while(readPacket(in)>0);
		
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
	private int readPacket(ByteBufferWrap buf)
	{
		buf.clear();
		try
		{
			int read = buf.readPacket(channel);
			numPackets++;
			numPacketsReceived++;
			if (PRINT_PACKETS)
				System.out.println("\nGot Packet-number: " + numPackets + ", " + buf.position() + " bytes.");
			return read;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * Note that this method clears buf.
	 * @param buf The buffer from which the output should be sent.
	 */
	private void sendPacket(ByteBufferWrap buf)
	{
		if (buf.remaining() <= 0) return;
		
		try
		{
			buf.sendPacket(channel, server_address);	
			numPackets++;
			buf.clear();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	private void putVerify(ByteBufferWrap buf, String real_name, String nick)
	{
		//buf.clear();
		
		buf.putByte(PKT_VERIFY);
		buf.putString(real_name);
		buf.putString(nick);
		buf.putString(DISPLAY);
	}
	
	private void sendVerify(ByteBufferWrap buf, String real_name, String nick)
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

	private void sendAck(ByteBufferWrap buf, Ack ack)
	{
		putAck(buf, ack);
		if(PRINT_PACKETS)
			System.out.println("\n"+ack);
	}
	
	public void sendAck(Ack ack)
	{
		sendAck(out, ack);
	}
	
	private  void putPower(ByteBufferWrap buf, short power)
	{
		buf.putByte(PKT_POWER);
		buf.putShort((short)(power*256));
	}
	private  void putPowerS(ByteBufferWrap buf, short power)
	{
		buf.putByte(PKT_POWER_S);
		buf.putShort((short)(power*256));
	}
	
	private  void putTurnSpeed(ByteBufferWrap buf, short turn_speed)
	{
		buf.putByte(PKT_TURNSPEED);
		buf.putShort((short)(turn_speed*256));
	}
	private  void putTurnSpeedS(ByteBufferWrap buf, short turn_speed)
	{
		buf.putByte(PKT_TURNSPEED_S);
		buf.putShort((short)(turn_speed*256));
	}
	private  void putTurnResistance(ByteBufferWrap buf, short turn_resistance)
	{
		buf.putByte(PKT_TURNRESISTANCE);
		buf.putShort((short)(turn_resistance*256));
	}
	private  void putTurnResistanceS(ByteBufferWrap buf, short turn_resistance)
	{
		buf.putByte(PKT_TURNRESISTANCE_S);
		buf.putShort((short)(turn_resistance*256));
	}
	private  void putDisplay(ByteBufferWrap buf)
	{
		buf.putByte(PKT_DISPLAY);
		buf.putShort(WIDTH_WANTED);
		buf.putShort(HEIGHT_WANTED);
		buf.putByte(NUM_SPARK_COLORS);
		buf.putByte(SPARK_RAND);
	}
	private  void putMOTDRequest(ByteBufferWrap buf)
	{
		buf.putByte(PKT_MOTD);
		buf.putBytes(MOTD_BYTES);
	}
	
	private  void putFPSRequest(ByteBufferWrap buf, byte max_fps)
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
	private void netStart(ByteBufferWrap out)
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
	
	
	private  ReliableDataError getReliableData(ReliableData data, ByteBufferWrap in)
	{
		in.clear();
		readPacket(in);
		//in.flip();
		
		return data.readReliableData(in, this);
	}
	
	private ReplyMessage getReplyMessage(ByteBufferWrap buf, ReplyMessage message)
	{
		readPacket(buf);
		//buf.flip();
		return ReplyMessage.readReplyMessage(buf, message);
	}
	
	private  int readMapPacket(ByteBufferWrap in, ByteBufferWrap map, ReliableData reliable)
	{
		//readReliableData(reliable, in);
		int remaining = in.remaining();
		System.out.println("Reliable len = "+reliable.getLen()+"\nMapPacket remaining = " + remaining);
		map.putBytes(in);
		return remaining;
	}
	
	private int getMapPacket(ByteBufferWrap in, ByteBufferWrap map, ReliableData reliable)
	{
		ReliableDataError error = getReliableData(reliable, in);
		
		if (error == ReliableDataError.NO_ERROR)
		{
			sendPacket(out);
			return readMapPacket(in, map, reliable);
		}
		
		return -1;
		
		//sendAck(out, Ack.ack.setAck(reliable));
		//System.out.println(reliable);
	}
	
	private  int readFirstMapPacket(ByteBufferWrap in, ByteBufferWrap map, BlockMapSetup setup, ReliableData reliable)
	{
		setup.readMapSetup(in);
		int remaining = in.remaining();
		System.out.println("Reliable len = "+reliable.getLen()+"\nFirstMapPacket remaining = " + remaining);
		map.putBytes(in);
		return remaining;
	}
	
	private  int getFirstMapPacket(ByteBufferWrap in, ByteBufferWrap map, BlockMapSetup setup, ReliableData reliable)
	{
		ReliableDataError error = getReliableData(reliable, in);
		
		while(error!=ReliableDataError.NO_ERROR)
		{
			System.out.println("Didn't get reliable data when waiting for map\n" + error + "\n" + reliable);
			error = getReliableData(reliable, in);
		}
		
		sendPacket(out);
		
		return readFirstMapPacket(in, map, setup, reliable);		
	}
	
	private void putKeyboard(ByteBufferWrap buf, BitVector keyboard)
	{		
		buf.putByte(PKT_KEYBOARD);
		buf.putInt(last_keyboard_change);
		
		synchronized(keyboard)
		{	
			buf.putBytes(keyboard.getBytes());
		}
		
		last_keyboard_change++;
	}
	
	public void movePointer(short amount)
	{
		pointer_move_amount += amount;
	}
	
	private void putPointerMove(ByteBufferWrap buf)
	{
		if(pointer_move_amount!=0)
		{
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
	private void netSetup(ByteBufferWrap in, ByteBufferWrap map, BlockMapSetup setup, ReliableData reliable)
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
		
		if (setup.getMapOrder() != BlockMapSetup.SETUP_MAP_UNCOMPRESSED)
		{
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
				System.out.println("**********Unsuported type: " + type + "************");
				break;
			}
		}

		reliableBuf.setReading();

		while (reliableBuf.remaining()>0)
		{
			if(PRINT_PACKETS)System.out.println("\nAttempting to read from reliableBuf");

			short type = reliableBuf.peekByte();

			if(readers[type]!=null)
			{
				int pos = reliableBuf.position();
				try
				{
					readers[type].processPacket(reliableBuf, client);
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
		
		if (reliableBuf.remaining()<=0)
		{
			reliableBuf.clear();
		}
	}
	
	private interface PacketProcessor
	{
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
	 * Stops input loop and sends quit packets.
	 */
	public void quit()
	{
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
		
		long runTime = (System.currentTimeMillis()-startTime)/1000;
		
		System.out.println("\nClient ran for " + runTime + " seconds.\n" +
							numPacketsReceived + " packets were received.\n" +
							numFrames + " frames were received.\n" +
							"Average fps = " + (double)numFrames/runTime);
		
		
	}
	
	
	//other methods client might need
	public BitVector getKeyboard()
	{
		synchronized(keyboard)
		{
			return keyboard;
		}
	}

}
