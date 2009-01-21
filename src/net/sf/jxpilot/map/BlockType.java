package net.sf.jxpilot.map;

import static net.sf.jxpilot.util.Utilities.getUnsignedByte;

public enum BlockType {

	SPACE,
	FILLED,
	FILLED_NO_DRAW,
	FUEL,
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
	TREASURE;
	
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
	public static final byte SETUP_CHECK	= 			80;	/* + check point number (26) */
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
	public static final byte SETUP_LEFT_GRAV	 = 		124;
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
}
