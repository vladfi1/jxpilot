package net.sf.jxpilot.test;

class MapSetup
{
	
	/*
	 * Definitions to tell the client how the server has been setup.
	 */

	/*
	 * If the high bit of a map block is set then the next block holds
	 * the number of contiguous map blocks that have the same block type.
	 */
	public static final byte SETUP_COMPRESSED = (byte) 0x80;

	/*
	 * Tell the client how and if the map is compressed.
	 */
	
	public static final byte  SETUP_MAP_ORDER_XY = 1;
	public static final byte SETUP_MAP_ORDER_YX	= 1;
	public static final byte SETUP_MAP_UNCOMPRESSED = 3;
	
	/*
	 * Definitions for the map layout which permit a compact definition
	 * of map data.
	 */
	public static final byte SETUP_SPACE =				0;
	public static final byte SETUP_FILLED = 			1;
	public static final byte SETUP_FILLED_NO_DRAW = 	2;
	public static final byte SETUP_FUEL= 				3;
	public static final byte SETUP_REC_RU = 			4;
	public static final byte SETUP_REC_RD = 			5;
	public static final byte SETUP_REC_LU = 			6;
	public static final byte SETUP_REC_LD = 			7;
	public static final byte  SETUP_ACWISE_GRAV = 		8;
	public static final byte  SETUP_CWISE_GRAV = 		9;
	public static final byte  SETUP_POS_GRAV = 			10;
	public static final byte  SETUP_NEG_GRAV = 			11;
	public static final byte  SETUP_WORM_NORMAL = 		12;
	public static final byte  SETUP_WORM_IN = 			13;
	public static final byte  SETUP_WORM_OUT = 			14;
	public static final byte  SETUP_CANNON_UP	= 		15;
	public static final byte  SETUP_CANNON_RIGHT = 		16;
	public static final byte  SETUP_CANNON_DOWN = 		17;
	public static final byte  SETUP_CANNON_LEFT = 		18;
	public static final byte  SETUP_SPACE_DOT	= 		19;
	public static final byte  SETUP_TREASURE	 = 		20;	/* + team number (10) */
	public static final byte  SETUP_BASE_LOWEST = 		30;	/* lowest base number */
	public static final byte  SETUP_BASE_UP = 			30;	/* + team number (10) */
	public static final byte  SETUP_BASE_RIGHT = 		40;	/* + team number (10) */
	public static final byte  SETUP_BASE_DOWN	= 		50;	/* + team number (10) */
	public static final byte  SETUP_BASE_LEFT	= 		60;	/* + team number (10) */
	public static final byte  SETUP_BASE_HIGHEST = 		69;	/* highest base number */
	public static final byte  SETUP_TARGET = 			70;	/* + team number (10) */
	public static final byte  SETUP_CHECK	= 			80;	/* + check point number (26) */
	public static final byte  SETUP_ITEM_CONCENTRATOR = 110;
	public static final byte  SETUP_DECOR_FILLED = 		111;
	public static final byte  SETUP_DECOR_RU = 			112;
	public static final byte  SETUP_DECOR_RD = 			113;
	public static final byte  SETUP_DECOR_LU = 			114;
	public static final byte  SETUP_DECOR_LD = 			115;
	public static final byte  SETUP_DECOR_DOT_FILLED =	116;
	public static final byte  SETUP_DECOR_DOT_RU = 		117;
	public static final byte  SETUP_DECOR_DOT_RD = 		118;
	public static final byte  SETUP_DECOR_DOT_LU = 		119;
	public static final byte  SETUP_DECOR_DOT_LD = 		120;
	public static final byte  SETUP_UP_GRAV = 			121;
	public static final byte  SETUP_DOWN_GRAV = 		122;
	public static final byte  SETUP_RIGHT_GRAV = 		123;
	public static final byte  SETUP_LEFT_GRAV	 = 		124;
	public static final byte  SETUP_ASTEROID_CONCENTRATOR = 125;

	public static final byte  BLUE_UP =			0x01;
	public static final byte  BLUE_RIGHT =		0x02;
	public static final byte  BLUE_DOWN	=		0x04;
	public static final byte  BLUE_LEFT	=		0x08;
	public static final byte  BLUE_OPEN	=		0x10;		/* diagonal botleft -> rightup */
	public static final byte  BLUE_CLOSED =		0x20;		/* diagonal topleft -> rightdown */
	public static final byte  BLUE_FUEL	=		0x30;		/* when filled block is fuelstation */
	public static final byte  BLUE_BELOW =		0x40;		/* when triangle is below diagonal */
	public static final byte  BLUE_BIT =		(byte)0x80;	/* set when drawn with blue lines */

	public static final byte  DECOR_LEFT =		0x01;
	public static final byte  DECOR_RIGHT =		0x02;
	public static final byte  DECOR_DOWN =		0x04;
	public static final byte  DECOR_UP =		0x08;
	public static final byte  DECOR_OPEN =		0x10;
	public static final byte  DECOR_CLOSED =	0x20;
	public static final byte  DECOR_BELOW =		0x40;

	/*
	 * Convert a `space' map block into a dot.
	 */
	/*
	static byte Map_make_dot(byte data)
	{
	    if (data == SETUP_SPACE) {
		return SETUP_SPACE_DOT;
	    }
	    else if (data == SETUP_DECOR_FILLED) {
		return SETUP_DECOR_DOT_FILLED;
	    }
	    else if (data == SETUP_DECOR_RU) {
		return SETUP_DECOR_DOT_RU;
	    }
	    else if (data == SETUP_DECOR_RD) {
		return SETUP_DECOR_DOT_RD;
	    }
	    else if (data == SETUP_DECOR_LU) {
		return SETUP_DECOR_DOT_LU;
	    }
	    else if (data == SETUP_DECOR_LD) {
		return SETUP_DECOR_DOT_LD;
	    }
	}
	*/
	
	
	//map setup variables
	private int map_data_len, mode;
	private short lives, x, y, frames_per_second, map_order;
	private String name, author;
	private byte[] map_data;
	
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
	
	public void setMapData(byte[] data)
	{map_data = data;}
	public byte[] getMapData(){return map_data;}
	
	public void setMapOrder(short order){map_order = order;}
	
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
	
	public void printMapData()
	{
		for (int y = this.y-1;y>=0;y--)
		{
			for (int x =0;x<this.x; x++)
			{
				System.out.print(getBlock(getBlock(x,y)));
			}
			System.out.println();
		}
	}
	
	public byte getBlock(int x, int y)
	{	
		return map_data[y + x*this.y];
	}
	
	private String getBlock(byte block)
	{
		switch(block)
		{
		case SETUP_SPACE: return " ";
		case SETUP_FILLED: return "F";
		case SETUP_REC_RU: return "\\";
		case SETUP_REC_RD: return "/";
		case SETUP_REC_LU: return "/";
		case SETUP_REC_LD: return "\\";
		
		}
		
		if (block>= SETUP_TREASURE && block <SETUP_TREASURE + 10)
		{
			return "O";
		}
		
		if (block >= SETUP_BASE_LOWEST && block <= SETUP_BASE_HIGHEST)
		{
			return "B";
		}
		
		//return String.valueOf(block);
		return "?";
	}
}