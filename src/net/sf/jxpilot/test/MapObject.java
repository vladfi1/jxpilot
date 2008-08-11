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
	
	public MapObject(byte type, Shape s)
	{
		this(type);
		shape = s;
	}
	
	public MapObject(byte type)
	{
		this.type = type;
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
	
	static MapObject getBlockShape(byte block_data)
	{
		switch(block_data)
		{
		case SETUP_SPACE: return new MapObject(SETUP_SPACE);
		case SETUP_FILLED: return new MapObject(SETUP_FILLED, new Rectangle(DEFAULT_WIDTH,DEFAULT_HEIGHT));
		case SETUP_FUEL: return  new MapObject(SETUP_FUEL, new Rectangle(DEFAULT_WIDTH,DEFAULT_HEIGHT));
		case SETUP_REC_RU: return new MapObject(SETUP_REC_RU, getRec(true, true));
		case SETUP_REC_RD: return new MapObject(SETUP_REC_RD, getRec(true, false));
		case SETUP_REC_LU: return new MapObject(SETUP_REC_LU, getRec(false, true));
		case SETUP_REC_LD: return new MapObject(SETUP_REC_LD, getRec(false, false));
		case SETUP_WORM_NORMAL: return new MapObject(SETUP_WORM_NORMAL);
		case SETUP_WORM_IN: return new MapObject(SETUP_WORM_IN);
		case SETUP_WORM_OUT: return new MapObject(SETUP_WORM_OUT) ;
		}
		
		if (block_data>= SETUP_TREASURE && block_data <SETUP_TREASURE + 10)
		{
			return new MapObject(block_data, new Ellipse2D.Float(0,0,DEFAULT_WIDTH, DEFAULT_HEIGHT));
		}
		
		if (block_data >= SETUP_BASE_LOWEST && block_data <= SETUP_BASE_HIGHEST)
		{
			return new MapObject(block_data, new Line2D.Float(0,DEFAULT_HEIGHT,DEFAULT_WIDTH, DEFAULT_HEIGHT));
		}
		
		return new MapObject(block_data);
	}
	
	private static Polygon getRec(boolean right, boolean up)
	{
		Polygon poly = new Polygon();
		if (right)
		{
			if (up)
			{
				poly.addPoint(DEFAULT_WIDTH, 0);
				poly.addPoint(DEFAULT_WIDTH, DEFAULT_HEIGHT);
				poly.addPoint(0, 0);
				
				
			}
			else
			{
				poly.addPoint(0, DEFAULT_HEIGHT);
				poly.addPoint(DEFAULT_WIDTH, 0);
				poly.addPoint(DEFAULT_WIDTH, DEFAULT_HEIGHT);
				
			}
		}
		else
		{
			if (up)
			{

				poly.addPoint(0, DEFAULT_HEIGHT);
				poly.addPoint(DEFAULT_WIDTH, 0);
				poly.addPoint(0, 0);
			}
			else
			{
				poly.addPoint(0, DEFAULT_HEIGHT);
				poly.addPoint(DEFAULT_WIDTH, DEFAULT_HEIGHT);
				poly.addPoint(0, 0);
				
			}
		}
		
		return poly;
	}
}
