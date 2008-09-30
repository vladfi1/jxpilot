package net.sf.jxpilot.game;

import java.awt.*;
import java.awt.geom.*;

/**
 * Holds all Player information.
 * @author vlad
 */
public class Player
{
	/**
	 * This id indicates that the player does not exist.
	 */
	public static final short NO_ID = -1;
	
	//player data
	private short id, my_team, my_char;
	private String nick, real, host;
	private ShipShape ship_shape;
	
	private short score, life;
	
	//ship data
	/**
	 * Whether or not to draw the ship.
	 */
	private boolean active=false;
	
	private ShipHolder ship;

	public Player(short id, short my_team, short my_char, String nick, String real, String host, ShipShape shape)
	{
		this.id = id;
		this.my_team = my_team;
		this.my_char = my_char;
		this.nick = nick;
		this.real = real;
		this.host = host;
		this.ship_shape = shape;
		
		ship = new ShipHolder();
	}
	
	//player data
	public short getId(){return id;}
	public short getTeam(){return my_team;}
	public short getChar(){return my_char;}
	public String getNick(){return nick;}
	public String getReal(){return real;}
	public String getHost(){return host;}
	public ShipShape getShipShape(){return ship_shape;}

	public void setScore(short score){this.score = score;}
	public void setLife(short life){this.life = life;}
	
	public short getScore(){return score;}
	public short getLife(){return life;}
	
	//ship data
	public Polygon getShape(){return ship_shape.getShape();}
	public void setActive(boolean b){active = b;}
	public boolean isActive(){return active;}
	public ShipHolder getShip(){return ship;}
	
	public void setShip(ShipHolder s)
	{
		active = true;
		ship.setFrom(s);
	}
	
	public String toString()
	{
		return nick + '(' + life+')' + ' ' + score;
	}
}