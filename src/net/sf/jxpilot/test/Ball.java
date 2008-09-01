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
	private AbstractClient client;
	private Connector connector;
	
	private Ball(){}
	public Ball(AbstractClient client)
	{
		this.client = client;
		connector = new Connector();
	}
	
	/**
	 * @return A new ball not to be used for drawing.
	 */
	public static Ball createHolder()
	{
		return new Ball();
	}
	
	public Ball setBall(short x, short y, short id)
	{
		this.x = x;
		this.y = y;
		this.id = id;
		return this;
	}
	
	public void set(Ball other)
	{
		setBall(other.x, other.y, other.id);
	}
	
	public Ball getNewInstance()
	{
		return new Ball(client);
	}
	
	public short getX(){return x;}
	public short getY(){return y;}
	
	private void setConnector()
	{
		Player p = client.getPlayer(id);
		connector.setConnector(x, y, (short)p.getX(), (short)p.getY(), (byte)0);
	}
	
	public void paintDrawable(Graphics2D g2d)
	{
		AffineTransform saved = g2d.getTransform();
		
		g2d.setColor(BALL_COLOR);
		g2d.translate(x, y);
		
		g2d.fill(BallShape);
		
		g2d.setTransform(saved);
		

		if (id>=0)
		{
			setConnector();
			connector.paintDrawable(g2d);
		}
	}
}