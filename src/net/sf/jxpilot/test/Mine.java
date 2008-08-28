package net.sf.jxpilot.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

public class Mine implements ExtendedDrawable<Mine>
{
	private static final Color MINE_COLOR = Color.CYAN;
	private static final int X_RADIUS = 10, Y_RADIUS = 5;
	private static final Ellipse2D.Float mineShape= new Ellipse2D.Float(-X_RADIUS, -Y_RADIUS, 2*X_RADIUS, 2*Y_RADIUS);
	
	private short x,y,id;
	private byte team_mine;
	
	public Mine setMine(short x, short y,byte team_mine, short id)
	{
		this.x = x;
		this.y = y;
		this.team_mine = team_mine;
		this.id = id;
		return this;
	}
	
	public Mine getNewInstance()
	{return new Mine();}
	
	public void setDrawable(Mine other)
	{
		this.setMine(other.x, other.y, other.team_mine, other.id);
	}
	
	public void paintDrawable(Graphics2D g2d)
	{
		AffineTransform saved = g2d.getTransform();
		
		g2d.setColor(MINE_COLOR);
		g2d.translate(x, y);
		g2d.fill(mineShape);
		
		g2d.setTransform(saved);
	}
}