package net.sf.jxpilot.test;

import java.awt.*;
import java.awt.geom.*;

/**
 * Class that holds balls as an ArrayList. Balls are held as a private inner class.
 * Balls are represented by an id, and an x/y.
 * 
 * @author vlad
 */
public class BallHandler extends java.util.ArrayList<BallHandler.Ball>
{
	//drawing info
	public static final int Ball_RADIUS = 10;
	private final Color BALL_COLOR = Color.GREEN;
	private final Ellipse2D BallShape = 
		new Ellipse2D.Float(-Ball_RADIUS,-Ball_RADIUS,2*Ball_RADIUS,2*Ball_RADIUS);
	
	public static final int DEFAULT_SIZE = 10;
	
	/**
	 * Represents the 1+the largest index used in the ArrayList.
	 * (The number of indexes used).
	 */
	private int size = 0;
	
	public BallHandler()
	{
		super(DEFAULT_SIZE);
		for (int i = 0;i<DEFAULT_SIZE;i++)
		{
			this.add(new Ball());
		}
		
		clearBalls();
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
	 * De-activates all the Balls in this ArrayList. This method only sets the size to 0,
	 * it does not actually set the Balls to inactive to save on time.
	 */
	public void clearBalls()
	{
		size = 0;
	}

	/**
	 * Adds a Ball to this BallHolder.

	 * @param x The x position of the ball.
	 * @param y The y position of the ball.
	 */
	public void addBall(short x, short y, short id)
	{
		if (size < super.size())
		{
			this.get(size).setBall(x, y, id);
		}
		else
		{
			this.add(new Ball().setBall(x, y, id));
			System.out.println("Increasing Balls size!");
		}
		size++;
	}
	
	/**
	 * Simple class for storing a Ball.
	 * @author vlad
	 */
	private class Ball implements Drawable
	{
		private short x,y;
		private short id;
		
		public Ball setBall(short x, short y, short id)
		{
			this.x = x;
			this.y = y;
			this.id = id;
			return this;
		}
		
		public short getX(){return x;}
		public short getY(){return y;}
		
		public void paintDrawable(Graphics2D g2d)
		{
			AffineTransform saved = g2d.getTransform();
			
			g2d.setColor(BALL_COLOR);
			g2d.translate(x, y);
			
			g2d.fill(BallShape);
			
			g2d.setTransform(saved);
		}		
	}
}