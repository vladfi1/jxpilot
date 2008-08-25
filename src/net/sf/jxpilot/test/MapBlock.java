package net.sf.jxpilot.test;

import java.awt.geom.*;
import java.awt.*;
import static net.sf.jxpilot.test.MapSetup.*;

/**
 * Represents a block of a BlockMap.
 * @author vlad
 *
 */
public class MapBlock implements java.io.Serializable
{
	public static final int BLOCK_SIZE = 35;
	
	public static final Color BLOCK_COLOR = Color.BLUE;
	public static final Color TREASURE_COLOR = Color.RED;
	public static final Color BASE_COLOR = Color.WHITE;
	public static final Color FUEL_COLOR = Color.RED;
	public static final Color WORMHOLE_COLOR = Color.MAGENTA;
	
	private byte type;
	private int x,y;
	private Shape shape;
	private Color color;
	private boolean fill=false;
	
	private MapBlock(byte type, int x, int y, Shape s, Color c, boolean fill)
	{
		this(type, x, y);
		shape = s;
		color = c;
		this.fill = fill;
	}
	
	private MapBlock(byte type, int x, int y)
	{
		this.type = type;
		this.x= x;
		this.y = y;
	}
	
	public int getX(){return x;}
	public int getY(){return y;}
	public byte getType(){return type;}
	public Shape getShape(){return shape;}
	public Color getColor(){return color;}
	public boolean isFilled(){return fill;}
	
	private static Rectangle getDefaultRectangle()
	{
		return new Rectangle(0,0, BLOCK_SIZE,BLOCK_SIZE);
	}
	
	private static Ellipse2D getDefaultCircle()
	{
		return new Ellipse2D.Float(0,0, BLOCK_SIZE,BLOCK_SIZE);
	}
	
	
	static MapBlock getMapBlock(byte block_data, int x, int y)
	{
		switch(block_data)
		{
		case SETUP_SPACE: return new MapBlock(SETUP_SPACE, x, y);
		case SETUP_FILLED: return new MapBlock(SETUP_FILLED, x, y, getDefaultRectangle(), BLOCK_COLOR, true);
		case SETUP_FUEL: return  new MapBlock(SETUP_FUEL, x, y, getDefaultRectangle(), FUEL_COLOR, true);
		case SETUP_REC_RU: return new MapBlock(SETUP_REC_RU, x, y, getRec(x, y, true, true), BLOCK_COLOR, true);
		case SETUP_REC_RD: return new MapBlock(SETUP_REC_RD, x, y, getRec(x, y, true, false), BLOCK_COLOR, true);
		case SETUP_REC_LU: return new MapBlock(SETUP_REC_LU, x, y, getRec(x, y, false, true), BLOCK_COLOR, true);
		case SETUP_REC_LD: return new MapBlock(SETUP_REC_LD, x, y, getRec(x, y, false, false), BLOCK_COLOR, true);
		case SETUP_WORM_NORMAL: return new MapBlock(SETUP_WORM_NORMAL, x, y, getDefaultCircle(), WORMHOLE_COLOR, true);
		case SETUP_WORM_IN: return new MapBlock(SETUP_WORM_IN, x, y, getDefaultCircle(), WORMHOLE_COLOR, true);
		case SETUP_WORM_OUT: return new MapBlock(SETUP_WORM_OUT, x, y, getDefaultCircle(), WORMHOLE_COLOR, true) ;
		}
		
		if (block_data>= SETUP_TREASURE && block_data <SETUP_TREASURE + 10)
		{
			return new MapBlock(block_data, x, y, new Ellipse2D.Float(0,0,BLOCK_SIZE, BLOCK_SIZE), TREASURE_COLOR,false);
		}
		
		if (block_data >= SETUP_BASE_LOWEST && block_data <= SETUP_BASE_HIGHEST)
		{
			if (block_data>= SETUP_BASE_DOWN && block_data < SETUP_BASE_DOWN +10)
			{
				return new MapBlock(block_data, x, y, 
						new Line2D.Float(0,BLOCK_SIZE,BLOCK_SIZE, BLOCK_SIZE), BASE_COLOR, false);
			}
			
			return new MapBlock(block_data, x, y, 
					new Line2D.Float(0, 0, BLOCK_SIZE, 0), BASE_COLOR, false);
		}
		
		return new MapBlock(block_data, x, y);
	}
	
	public static int getScreenY(MapSetup setup, int y)
	{
		return (setup.getY()-y-1);
	}
	
	private static Polygon getRec(int x, int y, boolean right, boolean up)
	{
		Polygon poly = new Polygon();
		if (right)
		{
			if (up)
			{
				poly.addPoint(BLOCK_SIZE, 0);
				poly.addPoint(BLOCK_SIZE, BLOCK_SIZE);
				poly.addPoint(0,BLOCK_SIZE);
			}
			else
			{
				poly.addPoint(BLOCK_SIZE, 0);
				poly.addPoint(BLOCK_SIZE, BLOCK_SIZE);
				poly.addPoint(0, 0);
				
			}
		}
		else
		{
			if (up)
			{
				poly.addPoint(0, 0);
				poly.addPoint(BLOCK_SIZE, BLOCK_SIZE);
				poly.addPoint(0, BLOCK_SIZE);
			}
			else
			{
				poly.addPoint(0, 0);
				poly.addPoint(BLOCK_SIZE, 0);
				poly.addPoint(0, BLOCK_SIZE);	
			}
		}
		return poly;
	}
	
	public static String getBlockChar(byte block)
	{
		switch(block)
		{
		case SETUP_SPACE: return " ";
		case SETUP_FILLED: return "\0";
		case SETUP_FUEL: return "F";
		case SETUP_REC_RU: return "\\";
		case SETUP_REC_RD: return "/";
		case SETUP_REC_LU: return "/";
		case SETUP_REC_LD: return "\\";
		case SETUP_WORM_NORMAL:
		case SETUP_WORM_IN:
		case SETUP_WORM_OUT: return "W";
		}
		
		if (block>= SETUP_TREASURE && block <SETUP_TREASURE + 10)
		{
			return "O";
		}
		
		if (block >= SETUP_BASE_LOWEST && block <= SETUP_BASE_HIGHEST)
		{
			return "B";
		}
		
		//return String.valueOf(block);
		return "?";
	}
	
	public String toString()
	{
		return getBlockChar(type);
	}
}