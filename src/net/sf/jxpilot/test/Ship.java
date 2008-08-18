package net.sf.jxpilot.test;

import java.awt.*;

public class Ship implements Drawable
{
	public static final Color SHIP_COLOR = Color.WHITE;
	
	private int x, y, heading;
	private int id;
	private ShipShape shipshape;
	
	public Ship(ShipShape shape, int id)
	{
		this.shipshape = shape;
		this.id = id;
	}
	
	public Polygon getShape(){return shipshape.getShape();}
	public int getX(){return x;}
	public int getY(){return y;}
	public int getHeading(){return heading;}
	public Color getColor(){return SHIP_COLOR;}
	public boolean isFilled(){return false;}
	public int getId(){return id;}
	
	public void setShip(int x, int y, int heading)
	{
		this.x = x;
		this.y =y;
		this.heading = heading;
	}
	
	public void paintDrawable(Graphics2D g2d)
	{
		//g2d.rotate(heading, x, y);
		g2d.translate(x, y);
		g2d.rotate(heading);
		g2d.draw(getShape());
		g2d.rotate(-heading);
		g2d.translate(-x,-y);
		//g2d.rotate(-heading, -x, -y);
	}
}
