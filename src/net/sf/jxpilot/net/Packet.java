package net.sf.jxpilot.net;

/**
 * Holds various constants used in network communications.
 */
public class Packet {
	
	public static final byte ENTER_QUEUE_pack =	0x01;
	public static final byte ENTER_GAME_pack = 	0x00;
	public static final byte SUCCESS = 			0x00;
	
	/* before version 3.8.0 this was 8 bytes. */
	public static final byte KEYBOARD_SIZE	=9;

	/*
	 * Definition of various client/server packet types.
	 */

	/* packet types: 0 - 9 */
	public static final byte PKT_UNDEFINED	=	0;
	public static final byte PKT_VERIFY		=	1;
	public static final byte PKT_REPLY		=	2;
	public static final byte PKT_PLAY		=	3;
	public static final byte PKT_QUIT		=	4;
	public static final byte PKT_MESSAGE	=	5;
	public static final byte PKT_START		=	6;
	public static final byte PKT_END		=	7;
	public static final byte PKT_SELF		=	8;
	public static final byte PKT_DAMAGED	=	9;

	/* packet types: 10 - 19 */
	public static final byte PKT_CONNECTOR	=	10;
	public static final byte PKT_REFUEL		=	11;
	public static final byte PKT_SHIP		=	12;
	public static final byte PKT_ECM		=	13;
	public static final byte PKT_PAUSED		=	14;
	public static final byte PKT_ITEM		=	15;
	public static final byte PKT_MINE		=	16;
	public static final byte PKT_BALL		=	17;
	public static final byte PKT_MISSILE	=	18;
	public static final byte PKT_SHUTDOWN	=	19;

	/* packet types: 20 - 29 */
	public static final byte PKT_STRING		=	20;
	public static final byte PKT_DESTRUCT	=	21;
	public static final byte PKT_RADAR		=	22;
	public static final byte PKT_TARGET		=	23;
	public static final byte PKT_KEYBOARD	=	24;
	public static final byte PKT_SEEK		=	25;
	public static final byte PKT_SELF_ITEMS	=	26;	/* still under development */
	public static final byte PKT_TEAM_SCORE	=	27;	/* was PKT_SEND_BUFSIZE */
	public static final byte PKT_PLAYER		=	28;
	public static final byte PKT_SCORE		=	29;

	/* packet types: 30 - 39 */
	public static final byte PKT_FUEL		=	30;
	public static final byte PKT_BASE		=	31;
	public static final byte PKT_CANNON		=	32;
	public static final byte PKT_LEAVE		=	33;
	public static final byte PKT_POWER		=	34;
	public static final byte PKT_POWER_S	=	35;
	public static final byte PKT_TURNSPEED	=	36;
	public static final byte PKT_TURNSPEED_S=	37;
	public static final byte PKT_TURNRESISTANCE=38;
	public static final byte PKT_TURNRESISTANCE_S=	39;

	/* packet types: 40 - 49 */
	public static final byte PKT_WAR		=	40;
	public static final byte PKT_MAGIC	=		41;
	public static final byte PKT_RELIABLE	=	42;
	public static final byte PKT_ACK		=	43;
	public static final byte PKT_FASTRADAR	=	44;
	public static final byte PKT_TRANS		=	45;
	public static final byte PKT_ACK_CANNON	=	46;
	public static final byte PKT_ACK_FUEL	=	47;
	public static final byte PKT_ACK_TARGET	=	48;
	public static final byte PKT_SCORE_OBJECT=	49;

	/* packet types: 50 - 59 */
	public static final byte PKT_AUDIO		=	50;
	public static final byte PKT_TALK		=	51;
	public static final byte PKT_TALK_ACK	=	52;
	public static final byte PKT_TIME_LEFT	=	53;
	public static final byte PKT_LASER		=	54;
	public static final byte PKT_DISPLAY	=	55;
	public static final byte PKT_EYES		=	56;
	public static final byte PKT_SHAPE		=	57;
	public static final byte PKT_MOTD		=	58;
	public static final byte PKT_LOSEITEM	=	59;

	/* packet types: 60 - 69 */
	public static final byte PKT_NOT_USED_60	=	60;
	public static final byte PKT_NOT_USED_61	=	61;
	public static final byte PKT_NOT_USED_62	=	62;
	public static final byte PKT_NOT_USED_63	=	63;
	public static final byte PKT_NOT_USED_64	=	64;
	public static final byte PKT_NOT_USED_65	=	65;
	public static final byte PKT_NOT_USED_66	=	66;
	public static final byte PKT_NOT_USED_67	=	67;
	public static final byte PKT_MODIFIERS		=	68;
	public static final byte PKT_FASTSHOT		=	69;	/* replaces SHOT/TEAMSHOT */

	/* packet types: 70 - 79 */
	public static final byte PKT_THRUSTTIME		=	70;
	public static final byte PKT_MODIFIERBANK	=	71;
	public static final byte PKT_SHIELDTIME		=	72;
	public static final byte PKT_POINTER_MOVE	=	73;
	public static final byte PKT_REQUEST_AUDIO	=	74;
	public static final byte PKT_ASYNC_FPS		=	75;
	public static final byte PKT_TIMING			=	76;
	public static final byte PKT_PHASINGTIME	=	77;
	public static final byte PKT_ROUNDDELAY		=	78;
	public static final byte PKT_WRECKAGE		=	79;

	/* packet types: 80 - 89 */
	public static final byte PKT_ASTEROID		=	80;
	public static final byte PKT_WORMHOLE		=	81;
	public static final byte PKT_NOT_USED_82	=	82;
	public static final byte PKT_NOT_USED_83	=	83;
	public static final byte PKT_NOT_USED_84	=	84;
	public static final byte PKT_NOT_USED_85	=	85;
	public static final byte PKT_NOT_USED_86	=	86;
	public static final byte PKT_NOT_USED_87	=	87;
	public static final byte PKT_NOT_USED_88	=	88;
	public static final byte PKT_NOT_USED_89	=	89;

	/* packet types: 90 - 99 */
	/*
	 * Use these 10 packet type numbers for
	 * experimenting with new packet types.
	 */

	/* status reports: 101 - 102 */
	public static final byte PKT_FAILURE	=	101;
	public static final byte PKT_SUCCESS	=	102;

	/* optimized packet types: 128 - 255 */
	public static final byte PKT_DEBRIS		=	(byte)128;	/* + color + x + y */
	public static final int DEBRIS_TYPES	=	(8 * 4 * 4);//128
	
	public static final int NUM_PACKET_TYPES = 256;
}