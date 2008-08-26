package net.sf.jxpilot.test;

import java.awt.*;
import java.awt.geom.*;
//import java.util.*;

/**
 * Class that holds shots as an ArrayList. Shots are held as a private inner class.
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
	 * De-activates all the shots in this ArrayList.
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

	
	public void addShot(int type, short x, short y)
	{
		if (size < super.size())
		{
			this.get(size).setShot(type, x, y, true);
		}
		else
		{
			this.add(new Shot().setShot(type, x, y, true));
			System.out.println("Increasing shots size!");
		}
		size++;
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
		
		/**
		 * Whether or not to draw the shot.
		 */
		private boolean active=false;
		
		public Shot setShot(int type, short x, short y)
		{
			this.type = type;
			this.x = x;
			this.y = y;
			return this;
		}
		
		public Shot setShot(int type, short x, short y, boolean active)
		{
			setShot(type, x, y);
			setActive(active);
			return this;
		}
		
		public int getType(){return type;}
		public short getX(){return x;}
		public short getY(){return y;}
		public void setActive(boolean b){active = b;}
		public boolean isActive(){return active;}
		
		public void paintDrawable(Graphics2D g2d)
		{
			if (!active) return;
			
			AffineTransform saved = g2d.getTransform();
			
			g2d.setColor(ENEMY_COLOR);
			//g2d.rotate(heading, x, y);
			g2d.translate(x+selfX, y+selfY);
			
			g2d.fill(shotShape);
			//g2d.rotate(-heading);
			//g2d.translate(-x,-y);
			//g2d.rotate(-heading, -x, -y);
			g2d.setTransform(saved);
		}
		
	}
}
