package net.sf.jxpilot.test;

import java.awt.*;
import java.awt.geom.*;
//import java.util.*;

/**
 * Class that holds shots as an ArrayList. Shots are held as a private inner class.
 * Shots are represented by a type, and an x/y.
 * 
 * Types relative to ship
 * 
 * 26 = 0,0 
 * 25 = -1,0
 * 22 = 0,-1
 * 21 = -1,-1
 * 
 * +1 is one to the right
 * +4 is one up
 * 
 * 			28	29	30	31
 * 
 * 			24	25	26	26
 * 				  S
 * 			20	21	22	23
 * 
 * 			16	17	18	19
 * 
 * @author vlad
 */
public class ShotHandler extends java.util.ArrayList<ShotHandler.Shot>
{
	//drawing info
	public static final int SHOT_RADIUS = 2;
	private final Color TEAM_COLOR = Color.BLUE;
	private final Color ENEMY_COLOR = Color.WHITE;
	private final Ellipse2D shotShape = 
		new Ellipse2D.Float(-SHOT_RADIUS,-SHOT_RADIUS,2*SHOT_RADIUS,2*SHOT_RADIUS);
	
	public static final int DEFAULT_SIZE = 100;
	
	/**
	 * Represents the 1+the largest index used in the ArrayList.
	 * (The number of indexes used).
	 */
	private int size = 0;
	
	private short selfX, selfY;
	
	public ShotHandler()
	{
		super(DEFAULT_SIZE);
		for (int i = 0;i<DEFAULT_SIZE;i++)
		{
			this.add(new Shot());
		}
		
		clearShots();
	}
	
	public void setSelfPosition(short x, short y)
	{
		selfX = x;
		selfY = y;
	}
	
	/**
	 * Returns the number of elements currently used.
	 * 
	 * @override size() in ArrayList
	 */
	public int size()
	{
		return size;
	}
	
	
	/**
	 * De-activates all the shots in this ArrayList. This method only sets the size to 0,
	 * it does not actually set the shots to inactive to save on time.
	 */
	public void clearShots()
	{
		/*
		for (Shot s : this)
		{
			s.setActive(false);
		}
		*/
		
		size = 0;
	}

	/**
	 * Adds a shot to this ShotHolder.
	 * @param type Describes the area where the shot is. An area is a 256x256 square.
	 * 				Areas are positioned relative to the ship.
	 * @param x The x position within the area.
	 * @param y The y position within the area.
	 */
	public void addShot(int type, short x, short y)
	{
		if (size < super.size())
		{
			this.get(size).setShot(type, x, y);
		}
		else
		{
			this.add(new Shot().setShot(type, x, y));
			System.out.println("Increasing shots size!");
		}
		size++;
	}
	
	//methods/variables for dealing with areas
	/**
	 * 256 = 1+max value of an unsigned byte
	 */
	static final int AREA_SIZE = 256;
	static int getXArea(int type)
	{
		return type%4 - 2;
	}
	
	static int getYArea(int type)
	{
		return type / 4 - 6;
	}
	/**
	 * Simple class for storing a shot.
	 * @author vlad
	 */
	private class Shot implements Drawable
	{
		private int type;
		/**
		 * Unsigned byte.
		 */
		private short x,y;
		
		public Shot setShot(int type, short x, short y)
		{
			this.type = type;
			this.x = x;
			this.y = y;
			return this;
		}
		
		public int getType(){return type;}
		public short getX(){return x;}
		public short getY(){return y;}
		
		public void paintDrawable(Graphics2D g2d)
		{
			AffineTransform saved = g2d.getTransform();
			
			g2d.setColor(ENEMY_COLOR);
			g2d.translate(x+selfX+getXArea(type)*AREA_SIZE, y+selfY+getYArea(type)*AREA_SIZE);
			
			g2d.fill(shotShape);
			g2d.setTransform(saved);
		}
		
	}
}
