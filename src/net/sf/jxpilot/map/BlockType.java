package net.sf.jxpilot.map;

import static net.sf.jxpilot.util.Utilities.getUnsignedByte;

public enum BlockType {

	SPACE {
		@Override
		public Block createBlock(int num, int x, int y) {return new SpaceBlock(this, num, x, y);}
	},
	FILLED {
		@Override
		public Block createBlock(int num, int x, int y) {return new FilledBlock(this, num, x, y);}
	},
	FILLED_NO_DRAW,
	FUEL {
		//@Override
		//public Block createBlock(int num, int x, int y) {return new FuelBlock(num, x, y);}
	},
	REC_RU,
	REC_RD,
	REC_LU,
	REC_LD,
	ACWISE_GRAV,
	CWISE_GRAV,
	POS_GRAV,
	NEG_GRAV,
	WORM_NORMAL,
	WORM_IN,
	WORM_OUT,
	CANNON_UP,
	CANNON_RIGHT,
	CANNON_DOWN,
	CANNON_LEFT,
	SPACE_DOT,
	TREASURE_0,
	TREASURE_1,
	TREASURE_2,
	TREASURE_3,
	TREASURE_4,
	TREASURE_5,
	TREASURE_6,
	TREASURE_7,
	TREASURE_8,
	TREASURE_9,
	BASE_UP_0,
	BASE_UP_1,
	BASE_UP_2,
	BASE_UP_3,
	BASE_UP_4,
	BASE_UP_5,
	BASE_UP_6,
	BASE_UP_7,
	BASE_UP_8,
	BASE_UP_9,
	BASE_RIGHT_0,
	BASE_RIGHT_1,
	BASE_RIGHT_2,
	BASE_RIGHT_3,
	BASE_RIGHT_4,
	BASE_RIGHT_5,
	BASE_RIGHT_6,
	BASE_RIGHT_7,
	BASE_RIGHT_8,
	BASE_RIGHT_9,
	BASE_DOWN_0,
	BASE_DOWN_1,
	BASE_DOWN_2,
	BASE_DOWN_3,
	BASE_DOWN_4,
	BASE_DOWN_5,
	BASE_DOWN_6,
	BASE_DOWN_7,
	BASE_DOWN_8,
	BASE_DOWN_9,
	BASE_LEFT_0,
	BASE_LEFT_1,
	BASE_LEFT_2,
	BASE_LEFT_3,
	BASE_LEFT_4,
	BASE_LEFT_5,
	BASE_LEFT_6,
	BASE_LEFT_7,
	BASE_LEFT_8,
	BASE_LEFT_9,
	TARGET_0,
	TARGET_1,
	TARGET_2,
	TARGET_3,
	TARGET_4,
	TARGET_5,
	TARGET_6,
	TARGET_7,
	TARGET_8,
	TARGET_9,
	CHECK_0,
	CHECK_1,
	CHECK_2,
	CHECK_3,
	CHECK_4,
	CHECK_5,
	CHECK_6,
	CHECK_7,
	CHECK_8,
	CHECK_9,
	CHECK_10,
	CHECK_11,
	CHECK_12,
	CHECK_13,
	CHECK_14,
	CHECK_15,
	CHECK_16,
	CHECK_17,
	CHECK_18,
	CHECK_19,
	CHECK_20,
	CHECK_21,
	CHECK_22,
	CHECK_23,
	CHECK_24,
	CHECK_25,
	UNUSED_106,
	UNUSED_107,
	UNUSED_108,
	UNUSED_109,
	ITEM_CONCENTRATOR,
	DECOR_FILLED,
	DECOR_RU,
	DECOR_RD,
	DECOR_LU,
	DECOR_LD,
	DECOR_DOT_FILLED,
	DECOR_DOT_RU,
	DECOR_DOT_RD,
	DECOR_DOT_LU,
	DECOR_DOT_LD,
	UP_GRAV,
	DOWN_GRAV,
	RIGHT_GRAV,
	LEFT_GRAV,
	ASTEROID_CONCENTRATOR;
	
	public static final byte SETUP_SPACE =				0;
	public static final byte SETUP_FILLED = 			1;
	public static final byte SETUP_FILLED_NO_DRAW = 	2;
	public static final byte SETUP_FUEL= 				3;
	public static final byte SETUP_REC_RU = 			4;
	public static final byte SETUP_REC_RD = 			5;
	public static final byte SETUP_REC_LU = 			6;
	public static final byte SETUP_REC_LD = 			7;
	public static final byte SETUP_ACWISE_GRAV = 		8;
	public static final byte SETUP_CWISE_GRAV = 		9;
	public static final byte SETUP_POS_GRAV = 			10;
	public static final byte SETUP_NEG_GRAV = 			11;
	public static final byte SETUP_WORM_NORMAL = 		12;
	public static final byte SETUP_WORM_IN = 			13;
	public static final byte SETUP_WORM_OUT = 			14;
	public static final byte SETUP_CANNON_UP	= 		15;
	public static final byte SETUP_CANNON_RIGHT = 		16;
	public static final byte SETUP_CANNON_DOWN = 		17;
	public static final byte SETUP_CANNON_LEFT = 		18;
	public static final byte SETUP_SPACE_DOT	= 		19;
	public static final byte SETUP_TREASURE	 = 			20;	/* + team number (10) */
	/**
	 * The number of bases used for a certain base type. This is used for storing the team name.
	 */
	public static final byte NUM_BASES_PER_TYPE = 		10;
	public static final byte SETUP_BASE_LOWEST = 		30;	/* lowest base number */
	public static final byte SETUP_BASE_UP = 			30;	/* + team number (10) */
	public static final byte SETUP_BASE_RIGHT = 		40;	/* + team number (10) */
	public static final byte SETUP_BASE_DOWN	= 		50;	/* + team number (10) */
	public static final byte SETUP_BASE_LEFT	= 		60;	/* + team number (10) */
	public static final byte SETUP_BASE_HIGHEST = 		69;	/* highest base number */
	public static final byte SETUP_TARGET = 			70;	/* + team number (10) */
	public static final byte SETUP_CHECK =				80;	/* + check point number (26) */
	public static final byte SETUP_ITEM_CONCENTRATOR = 	110;
	public static final byte SETUP_DECOR_FILLED = 		111;
	public static final byte SETUP_DECOR_RU = 			112;
	public static final byte SETUP_DECOR_RD = 			113;
	public static final byte SETUP_DECOR_LU = 			114;
	public static final byte SETUP_DECOR_LD = 			115;
	public static final byte SETUP_DECOR_DOT_FILLED =	116;
	public static final byte SETUP_DECOR_DOT_RU = 		117;
	public static final byte SETUP_DECOR_DOT_RD = 		118;
	public static final byte SETUP_DECOR_DOT_LU = 		119;
	public static final byte SETUP_DECOR_DOT_LD = 		120;
	public static final byte SETUP_UP_GRAV = 			121;
	public static final byte SETUP_DOWN_GRAV = 			122;
	public static final byte SETUP_RIGHT_GRAV = 		123;
	public static final byte SETUP_LEFT_GRAV =			124;
	public static final byte SETUP_ASTEROID_CONCENTRATOR = 125;
	
	private static final BlockType[] VALUES = values();
	
	public static BlockType getBlock(byte setup_type) {
		return VALUES[getUnsignedByte(setup_type)];
	}
	
	/**
	 * The identifier for this block type in the map data.
	 */
	public final byte SETUP_TYPE;
	
	private BlockType(byte setup_type) {
		this.SETUP_TYPE = setup_type;
	}
	
	private BlockType() {
		this.SETUP_TYPE = (byte)this.ordinal();
	}
	
	public AbstractBlock createBlock(int num, int x, int y) {
		return null;
	}
}
