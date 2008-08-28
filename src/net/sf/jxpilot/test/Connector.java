package net.sf.jxpilot.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

public class Connector implements ExtendedDrawable<Connector>
{
	private final Color CONNECTOR_COLOR = Color.GREEN;
	private final Line2D.Float connectorShape= new Line2D.Float();
	
	private short x0,y0,x1,y1;
	private byte tractor;
	
	public Connector setConnector(short x0, short y0,short x1, short y1, byte tractor)
	{	
		this.x0 = x0;
		this.y0 = y0;
		this.x1 = x1;
		this.y1 = y1;
		this.tractor = tractor;
		return this;
	}
	
	public Connector getNewInstance()
	{return new Connector();}
	
	public void setDrawable(Connector other)
	{
		setConnector(other.x0, other.y0, other.x1, other.y1, other.tractor);
	}
	
	public void paintDrawable(Graphics2D g2d)
	{
		AffineTransform saved = g2d.getTransform();
		
		g2d.setColor(CONNECTOR_COLOR);
		
		connectorShape.setLine(x0, y0, x1, y1);
		g2d.draw(connectorShape);
		
		g2d.setTransform(saved);
	}
}