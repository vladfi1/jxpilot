package net.sf.jxpilot.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

public abstract class AbstractDebris<T extends AbstractDebris<T>>  implements ExtendedDrawable<T>
{
	//drawing info
	protected Color COLOR;
	protected Ellipse2D debrisShape;

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

	
	protected int type;
	/**
	 * Unsigned byte.
	 */
	private short x,y;
	private AbstractClient client;
	
	/**
	 * Use this if the AbstractDebris is only being used to pass information along.
	 */
	public AbstractDebris()
	{
		client = null;
	}
	
	/**
	 * Use this if the AbstractDebris is actually used for drawing.
	 * @param client
	 */
	public AbstractDebris(AbstractClient client)
	{
		this.client = client;
	}
	
	/**
	 * Used to instantiate subclasses so that they share the same client (since client is private).
	 * @param other Another AbstractDebris.
	 */
	protected void setClient(AbstractDebris<T> other)
	{
		client = other.client;
	}
	
	public void setAbstractDebris(int type, short x, short y)
	{
		this.type = type;
		this.x = x;
		this.y = y;
	}
	
	public void setDrawable(T other)
	{
		setAbstractDebris(other.type, other.x, other.y);
	}
	
	public int getType(){return type;}
	public short getX(){return x;}
	public short getY(){return y;}
	
	public void paintDrawable(Graphics2D g2d)
	{
		AffineTransform saved = g2d.getTransform();
		
		g2d.setColor(COLOR);
		g2d.translate(x+client.getSelfX()+getXArea(type)*AREA_SIZE, y+client.getSelfY()+getYArea(type)*AREA_SIZE);
		
		g2d.fill(debrisShape);
		
		g2d.setTransform(saved);
	}
}