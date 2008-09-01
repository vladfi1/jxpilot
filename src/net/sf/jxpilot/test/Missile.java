package net.sf.jxpilot.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class Missile implements ExtendedDrawable<Missile>
{
	public static final int MISSILE_WIDTH = 4;
	private static final Color MISSILE_COLOR = Color.WHITE;
	private static final Rectangle2D.Float missileShape= new Rectangle2D.Float();
	
	private short x,y;
	private short len, dir;
	
	public Missile setMissile(short x, short y,short len, short dir)
	{	
		this.x = x;
		this.y = y;
		this.len = len;
		this.dir = dir;
		return this;
	}
	
	public Missile getNewInstance()
	{return new Missile();}
	
	public void set(Missile other)
	{
		setMissile(other.x, other.y, other.len, other.dir);
	}
	
	public void paintDrawable(Graphics2D g2d)
	{
		AffineTransform saved = g2d.getTransform();
		
		g2d.setColor(MISSILE_COLOR);
		
		missileShape.setRect(-len/2, MISSILE_WIDTH/2, len, MISSILE_WIDTH);
		g2d.translate(x, y);
		g2d.rotate(Utilities.getAngleFrom128(dir));
		g2d.fill(missileShape);
		
		g2d.setTransform(saved);
	}
}