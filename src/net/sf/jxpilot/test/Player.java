package net.sf.jxpilot.test;

import java.awt.*;
import java.awt.geom.*;

/**
 * Holds all Player information including ship data.
 * @author vlad
 */
public class Player implements Drawable
{
	public static final int SHIP_RADIUS = 16;
	public static final Color SHIP_COLOR = Color.WHITE;
	
	//player data
	private short id, my_team, my_char;
	private String nick, real, host;
	private ShipShape ship_shape;
	
	//ship data
	/**
	 * Whether or not to draw the ship.
	 */
	private boolean active=false;
	private int x, y, heading;
	private Ellipse2D shield;
	private boolean shielded=false,
					cloaked=false,
					emergency_shield=false,
					phased=false,
					deflector=false;
	
	
	public Player(short id, short my_team, short my_char, String nick, String real, String host, ShipShape shape)
	{
		this.id = id;
		this.my_team = my_team;
		this.my_char = my_char;
		this.nick = nick;
		this.real = real;
		this.host = host;
		this.ship_shape = shape;
		
		shield = new Ellipse2D.Float();
		shield.setFrame(-SHIP_RADIUS, -SHIP_RADIUS, 2*SHIP_RADIUS, 2*SHIP_RADIUS);
	}
	
	//player data
	public short getId(){return id;}
	public short getTeam(){return my_team;}
	public short getChar(){return my_char;}
	public String getNick(){return nick;}
	public String getReal(){return real;}
	public String getHost(){return host;}
	public ShipShape getShipShape(){return ship_shape;}
	
	//ship data
	public Polygon getShape(){return ship_shape.getShape();}
	public int getX(){return x;}
	public int getY(){return y;}
	public int getHeading(){return heading;}
	public void setActive(boolean b){active = b;}
	public boolean isActive(){return active;}
	

	public void setShip(int x, int y, int heading, boolean shield, boolean cloak, boolean emergency_shield, boolean phased, boolean deflector)
	{
		this.x = x;
		this.y =y;
		this.heading = heading;
		this.shielded=shield;
		this.cloaked=cloak;
		this.emergency_shield=emergency_shield;
		this.phased=phased;
		this.deflector=deflector;
	}
	
	public void paintDrawable(Graphics2D g2d)
	{
		if (!active) return;
		
		AffineTransform saved = g2d.getTransform();
		
		g2d.setColor(SHIP_COLOR);
		//g2d.rotate(heading, x, y);
		g2d.translate(x, y);
		
		FontMetrics fm = g2d.getFontMetrics();
		
		Rectangle2D bounds = fm.getStringBounds(nick, g2d);
		
		//need to flip g2d so nick comes out ok
		g2d.scale(1, -1);
		g2d.drawString(nick, (float)-bounds.getWidth()/2, SHIP_RADIUS + (float)bounds.getHeight()/2);
		g2d.scale(1, -1);
		
		if (shielded)
		{
			g2d.draw(shield);
		}
		g2d.rotate(getAngleFrom128(heading));
		g2d.draw(getShape());
		//g2d.rotate(-heading);
		//g2d.translate(-x,-y);
		//g2d.rotate(-heading, -x, -y);
		g2d.setTransform(saved);
	}
	
	/**
	 * @param heading The angle in 128 degree mode.
	 * @return The angle in radian mode.
	 */
	public static double getAngleFrom128(int heading)
	{
		return (double)heading * Math.PI/64.0;
	}
}