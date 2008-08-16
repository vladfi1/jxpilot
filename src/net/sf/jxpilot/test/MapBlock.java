package net.sf.jxpilot.test;

import java.awt.geom.*;
import java.awt.geom.Ellipse2D.Double;
import java.awt.*;
import static net.sf.jxpilot.test.MapSetup.*;

public class MapBlock
{
	public static final int BLOCK_SIZE = 35;
	
	public static final Color BLOCK_COLOR = Color.BLUE;
	public static final Color TREASURE_COLOR = Color.RED;
	public static final Color BASE_COLOR = Color.WHITE;
	public static final Color FUEL_COLOR = Color.RED;
	
	private byte type;
	private int x,y;
	private Shape shape;
	private Color color;
	private boolean fill=false;
	
	public MapBlock(byte type, int x, int y, Shape s, Color c, boolean fill)
	{
		this(type, x, y);
		shape = s;
		color = c;
		this.fill = fill;
	}
	
	public MapBlock(byte type, int x, int y)
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
	public Color getColor(){return color;}
	public boolean isFilled(){return fill;}
	
	static MapBlock getBlockShape(MapSetup setup, byte block_data, int x, int y)
	{
		switch(block_data)
		{
		case SETUP_SPACE: return new MapBlock(SETUP_SPACE, x, y);
		case SETUP_FILLED: return new MapBlock(SETUP_FILLED, x, y, new Rectangle(x*BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE ,BLOCK_SIZE,BLOCK_SIZE), BLOCK_COLOR, true);
		case SETUP_FUEL: return  new MapBlock(SETUP_FUEL, x, y, new Rectangle(x*BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE ,BLOCK_SIZE,BLOCK_SIZE), FUEL_COLOR, true);
		case SETUP_REC_RU: return new MapBlock(SETUP_REC_RU, x, y, getRec(setup, x, y, true, true), BLOCK_COLOR, true);
		case SETUP_REC_RD: return new MapBlock(SETUP_REC_RD, x, y, getRec(setup, x, y, true, false), BLOCK_COLOR, true);
		case SETUP_REC_LU: return new MapBlock(SETUP_REC_LU, x, y, getRec(setup, x, y, false, true), BLOCK_COLOR, true);
		case SETUP_REC_LD: return new MapBlock(SETUP_REC_LD, x, y, getRec(setup, x, y, false, false), BLOCK_COLOR, true);
		case SETUP_WORM_NORMAL: return new MapBlock(SETUP_WORM_NORMAL, x, y);
		case SETUP_WORM_IN: return new MapBlock(SETUP_WORM_IN, x, y);
		case SETUP_WORM_OUT: return new MapBlock(SETUP_WORM_OUT, x, y) ;
		}
		
		if (block_data>= SETUP_TREASURE && block_data <SETUP_TREASURE + 10)
		{
			return new MapBlock(block_data, x, y, new Ellipse2D.Float(x*BLOCK_SIZE,getScreenY(setup, y) * BLOCK_SIZE,BLOCK_SIZE, BLOCK_SIZE), TREASURE_COLOR,false);
		}
		
		if (block_data >= SETUP_BASE_LOWEST && block_data <= SETUP_BASE_HIGHEST)
		{
			if (block_data>= SETUP_BASE_DOWN && block_data < SETUP_BASE_DOWN +10)
			{
				return new MapBlock(block_data, x, y, 
						new Line2D.Float(x*BLOCK_SIZE,getScreenY(setup, y) * BLOCK_SIZE,
						x*BLOCK_SIZE+BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE), BASE_COLOR, false);
			}
			
			return new MapBlock(block_data, x, y, 
					new Line2D.Float(x*BLOCK_SIZE,getScreenY(setup, y) * BLOCK_SIZE+BLOCK_SIZE,
					x*BLOCK_SIZE+BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE+BLOCK_SIZE), BASE_COLOR, false);
		}
		
		return new MapBlock(block_data, x, y);
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
				poly.addPoint(x*BLOCK_SIZE+BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE);
				poly.addPoint(x*BLOCK_SIZE+BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE + BLOCK_SIZE);
				poly.addPoint(x*BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE);
			}
			else
			{
				poly.addPoint(x*BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE+BLOCK_SIZE);
				poly.addPoint(x*BLOCK_SIZE+BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE);
				poly.addPoint(x*BLOCK_SIZE+BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE+BLOCK_SIZE);
				
			}
		}
		else
		{
			if (up)
			{

				poly.addPoint(x*BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE+BLOCK_SIZE);
				poly.addPoint(x*BLOCK_SIZE+BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE);
				poly.addPoint(x*BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE);
			}
			else
			{
				poly.addPoint(x*BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE+BLOCK_SIZE);
				poly.addPoint(x*BLOCK_SIZE+BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE+BLOCK_SIZE);
				poly.addPoint(x*BLOCK_SIZE, getScreenY(setup, y) * BLOCK_SIZE);	
			}
		}
		
		return poly;
	}
}
