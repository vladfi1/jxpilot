package net.sf.jxpilot.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;


/**
 * Simple class for storing a Ball.
 * @author vlad
 */
public class Ball implements ExtendedDrawable<Ball>
{
	public static final int Ball_RADIUS = 10;
	private static final Color BALL_COLOR = Color.GREEN;
	private static final Ellipse2D BallShape = 
		new Ellipse2D.Float(-Ball_RADIUS,-Ball_RADIUS,2*Ball_RADIUS,2*Ball_RADIUS);
	
	private short x,y;
	private short id;
	
	public Ball setBall(short x, short y, short id)
	{
		this.x = x;
		this.y = y;
		this.id = id;
		return this;
	}
	
	public Ball setDrawable(Ball other)
	{
		return setBall(other.x, other.y, other.id);
	}
	
	public Ball getNewInstance()
	{
		return new Ball();
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