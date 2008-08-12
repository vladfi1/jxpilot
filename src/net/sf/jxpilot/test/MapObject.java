package net.sf.jxpilot.test;

import java.awt.geom.*;
import java.awt.geom.Ellipse2D.Double;
import java.awt.*;
import static net.sf.jxpilot.test.MapSetup.*;

public class MapObject
{

	public static final int DEFAULT_WIDTH = 10, DEFAULT_HEIGHT = 10;
	
	private byte type;
	private int x,y;
	private Shape shape;
	
	public MapObject(byte type, int x, int y, Shape s)
	{
		this(type, x, y);
		shape = s;
	}
	
	public MapObject(byte type, int x, int y)
	{
		this.type = type;
		this.x= x;
		this.y = y;
	}
	
	public void setPosition(int x, int y)
	{
		this.x = x;
		this.y=y;
	}
	public int getX(){return x;}
	public int getY(){return y;}
	public byte getType(){return type;}
	public Shape getShape(){return shape;}
	
	static MapObject getBlockShape(MapSetup setup, byte block_data, int x, int y)
	{
		switch(block_data)
		{
		case SETUP_SPACE: return new MapObject(SETUP_SPACE, x, y);
		case SETUP_FILLED: return new MapObject(SETUP_FILLED, x, y, new Rectangle(x*DEFAULT_WIDTH, getScreenY(setup, y) * DEFAULT_WIDTH ,DEFAULT_WIDTH,DEFAULT_HEIGHT));
		case SETUP_FUEL: return  new MapObject(SETUP_FUEL, x, y, new Rectangle(x*DEFAULT_WIDTH, getScreenY(setup, y) * DEFAULT_WIDTH ,DEFAULT_WIDTH,DEFAULT_HEIGHT));
		case SETUP_REC_RU: return new MapObject(SETUP_REC_RU, x, y, getRec(setup, x, y, true, true));
		case SETUP_REC_RD: return new MapObject(SETUP_REC_RD, x, y, getRec(setup, x, y, true, false));
		case SETUP_REC_LU: return new MapObject(SETUP_REC_LU, x, y, getRec(setup, x, y, false, true));
		case SETUP_REC_LD: return new MapObject(SETUP_REC_LD, x, y, getRec(setup, x, y, false, false));
		case SETUP_WORM_NORMAL: return new MapObject(SETUP_WORM_NORMAL, x, y);
		case SETUP_WORM_IN: return new MapObject(SETUP_WORM_IN, x, y);
		case SETUP_WORM_OUT: return new MapObject(SETUP_WORM_OUT, x, y) ;
		}
		
		if (block_data>= SETUP_TREASURE && block_data <SETUP_TREASURE + 10)
		{
			return new MapObject(block_data, x, y, new Ellipse2D.Float(x*DEFAULT_WIDTH,getScreenY(setup, y) * DEFAULT_WIDTH,DEFAULT_WIDTH, DEFAULT_HEIGHT));
		}
		
		if (block_data >= SETUP_BASE_LOWEST && block_data <= SETUP_BASE_HIGHEST)
		{
			return new MapObject(block_data, x, y, 
					new Line2D.Float(x*DEFAULT_WIDTH,getScreenY(setup, y) * DEFAULT_WIDTH+DEFAULT_HEIGHT,
					x*DEFAULT_WIDTH+DEFAULT_WIDTH, getScreenY(setup, y) * DEFAULT_WIDTH+DEFAULT_HEIGHT));
		}
		
		return new MapObject(block_data, x, y);
	}
	
	public static int getScreenY(MapSetup setup, int y)
	{
		return (setup.getY()-y-1);
	}
	
	private static Polygon getRec(MapSetup setup, int x, int y, boolean right, boolean up)
	{
		Polygon poly = new Polygon();
		if (right)
		{
			if (up)
			{
				poly.addPoint(x*DEFAULT_WIDTH+DEFAULT_WIDTH, getScreenY(setup, y) * DEFAULT_WIDTH);
				poly.addPoint(x*DEFAULT_WIDTH+DEFAULT_WIDTH, getScreenY(setup, y) * DEFAULT_WIDTH + DEFAULT_HEIGHT);
				poly.addPoint(x*DEFAULT_WIDTH, getScreenY(setup, y) * DEFAULT_WIDTH);
			}
			else
			{
				poly.addPoint(x*DEFAULT_WIDTH, getScreenY(setup, y) * DEFAULT_WIDTH+DEFAULT_HEIGHT);
				poly.addPoint(x*DEFAULT_WIDTH+DEFAULT_WIDTH, getScreenY(setup, y) * DEFAULT_WIDTH);
				poly.addPoint(x*DEFAULT_WIDTH+DEFAULT_WIDTH, getScreenY(setup, y) * DEFAULT_WIDTH+DEFAULT_HEIGHT);
				
			}
		}
		else
		{
			if (up)
			{

				poly.addPoint(x*DEFAULT_WIDTH, getScreenY(setup, y) * DEFAULT_WIDTH+DEFAULT_HEIGHT);
				poly.addPoint(x*DEFAULT_WIDTH+DEFAULT_WIDTH, getScreenY(setup, y) * DEFAULT_WIDTH);
				poly.addPoint(x*DEFAULT_WIDTH, getScreenY(setup, y) * DEFAULT_WIDTH);
			}
			else
			{
				poly.addPoint(x*DEFAULT_WIDTH, getScreenY(setup, y) * DEFAULT_WIDTH+DEFAULT_HEIGHT);
				poly.addPoint(x*DEFAULT_WIDTH+DEFAULT_WIDTH, getScreenY(setup, y) * DEFAULT_WIDTH+DEFAULT_HEIGHT);
				poly.addPoint(x*DEFAULT_WIDTH, getScreenY(setup, y) * DEFAULT_WIDTH);	
			}
		}
		
		return poly;
	}
}
