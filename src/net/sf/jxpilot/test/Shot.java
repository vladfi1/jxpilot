package net.sf.jxpilot.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

/**
 * Simple class for storing a shot.
 * @author vlad
 */
public class Shot implements ExtendedDrawable<Shot>
{
	//drawing info
	public static final int SHOT_RADIUS = 2;
	private static final Color TEAM_COLOR = Color.BLUE;
	private static final Color ENEMY_COLOR = Color.WHITE;
	private static final Ellipse2D shotShape = 
		new Ellipse2D.Float(-SHOT_RADIUS,-SHOT_RADIUS,2*SHOT_RADIUS,2*SHOT_RADIUS);
	
	//methods/variables for dealing with areas
	/**
	 * 256 = 1+max value of an unsigned byte
	 */
	public static final int AREA_SIZE = 256;

	/**
	 * @param type The area type.
	 * @return The x-coordinate for the area: type % 4 - 2
	 */
	static int getXArea(int type)
	{
		return type%4 - 2;
	}

	/**
	 * @param type The area type.
	 * @return The y-coordinate for the area: type / 4 - 6
	 */
	static int getYArea(int type)
	{
		return type / 4 - 6;
	}

	
	private int type;
	/**
	 * Unsigned byte.
	 */
	private short x,y;
	private final AbstractClient client;
	
	/**
	 * Use this if the Shot is only being used to pass information along.
	 */
	public Shot()
	{
		client = null;
	}
	
	/**
	 * Use this if the shot is actually used for drawing.
	 * @param client
	 */
	public Shot(AbstractClient client)
	{
		this.client = client;
	}
	
	/**
	 * Generates a new Shot, with a reference to the same client.
	 */
	@Override
	public Shot getNewInstance()
	{
		return new Shot(client);
	}
	
	public Shot setShot(int type, short x, short y)
	{
		this.type = type;
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Shot setDrawable(Shot other)
	{
		return setShot(other.type, other.x, other.y);
	}
	
	public int getType(){return type;}
	public short getX(){return x;}
	public short getY(){return y;}
	
	public void paintDrawable(Graphics2D g2d)
	{
		AffineTransform saved = g2d.getTransform();
		
		g2d.setColor(ENEMY_COLOR);
		g2d.translate(x+client.getSelfX()+getXArea(type)*AREA_SIZE, y+client.getSelfY()+getYArea(type)*AREA_SIZE);
		
		g2d.fill(shotShape);
		
		g2d.setTransform(saved);
	}		
}