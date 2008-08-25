package net.sf.jxpilot.test;

import java.awt.*;
import java.awt.geom.*;

public class Ship implements Drawable
{
	public static final int SHIP_RADIUS = 16;
	public static final Color SHIP_COLOR = Color.WHITE;
	
	private int x, y, heading;
	private int id;
	private ShipShape shipshape;
	private String nick;
	
	public Ship(ShipShape shape, int id, String nick)
	{
		this.shipshape = shape;
		this.id = id;
		this.nick = nick;
	}
	
	public Polygon getShape(){return shipshape.getShape();}
	public int getX(){return x;}
	public int getY(){return y;}
	public int getHeading(){return heading;}
	public Color getColor(){return SHIP_COLOR;}
	public boolean isFilled(){return false;}
	public int getId(){return id;}
	public String getNick(){return nick;}
	
	public void setShip(int x, int y, int heading)
	{
		this.x = x;
		this.y =y;
		this.heading = heading;
	}
	
	public void paintDrawable(Graphics2D g2d)
	{
		AffineTransform saved = g2d.getTransform();
		
		g2d.setColor(SHIP_COLOR);
		//g2d.rotate(heading, x, y);
		g2d.translate(x, y);
		
		FontMetrics fm = g2d.getFontMetrics();
		
		Rectangle2D bounds = fm.getStringBounds(nick, g2d);
		
		g2d.scale(1, -1);
		g2d.drawString(nick, (float)-bounds.getWidth()/2, SHIP_RADIUS + (float)bounds.getHeight()/2);
		g2d.scale(1, -1);
		
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