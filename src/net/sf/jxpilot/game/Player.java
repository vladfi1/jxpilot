package net.sf.jxpilot.game;

import java.awt.*;

/**
 * Holds all Player information.
 * @see {@link PlayerHolder}
 * @author Vlad Firoiu
 */
public class Player extends PlayerHolder {
	/**
	 * This id indicates that the player does not exist.
	 */
	public static final short NO_ID = -1;
	
	private short score, life;
	
	//ship data
	/**
	 * Whether or not to draw the ship.
	 */
	private boolean active=false;
	
	private ShipHolder ship;
	
	public Player(short id, byte my_team, byte my_char, String nick, String real, String host, ShipShape shape) {
		super.setPlayer(id, my_team, my_char, nick, real, host, shape);
		ship = new ShipHolder();
	}
	
	public Player(PlayerHolder p) {
		super.setFrom(p);
		ship = new ShipHolder();
	}
	
	public void setScore(short score){this.score = score;}
	public void setLife(short life){this.life = life;}
	
	public short getScore(){return score;}
	public short getLife(){return life;}
	
	//ship data
	public Polygon getShape(){return ship_shape.getShape();}
	public void setActive(boolean b){active = b;}
	public boolean isActive(){return active;}
	public ShipHolder getShip(){return ship;}
	
	public void setShip(ShipHolder s) {
		active = true;
		ship.setFrom(s);
	}
	
	public String toString() {
		return name + '(' + life+')' + ' ' + score;
	}
}