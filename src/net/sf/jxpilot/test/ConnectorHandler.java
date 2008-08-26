package net.sf.jxpilot.test;

import java.awt.*;
import java.awt.geom.*;

/**
 * Class that holds Connectors as an ArrayList. Connectors are held as a private inner class.
 * Connectors are represented by a byte tractor, and an x0/y0/x1/y1 (shorts).
 * 
 * @author vlad
 */
public class ConnectorHandler extends java.util.ArrayList<ConnectorHandler.Connector>
{
	//drawing info
	private final Color Connector_COLOR = Color.GREEN;
	private final Line2D.Float ConnectorShape= new Line2D.Float();
	public static final int DEFAULT_SIZE = 10;
	
	/**
	 * Represents the 1+the largest index used in the ArrayList.
	 * (The number of indexes used).
	 */
	private int size = 0;
	
	public ConnectorHandler()
	{
		super(DEFAULT_SIZE);
		for (int i = 0;i<DEFAULT_SIZE;i++)
		{
			this.add(new Connector());
		}
		
		clearConnectors();
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
	 * De-activates all the Connectors in this ArrayList. This method only sets the size to 0,
	 * it does not actually set the Connectors to inactive to save on time.
	 */
	public void clearConnectors()
	{
		size = 0;
	}

	/**
	 * Adds a Connector to this ConnectorHolder.

	 * @param x The x position of the Connector.
	 * @param y The y position of the Connector.
	 */
	public void addConnector(short x0, short y0,short x1, short y1, byte tractor)
	{
		if (size < super.size())
		{
			this.get(size).setConnector(x0, y0,x1,y1, tractor);
		}
		else
		{
			this.add(new Connector().setConnector(x0, y0,x1,y1, tractor));
			System.out.println("Increasing Connectors size!");
		}
		size++;
	}
	
	/**
	 * Simple class for storing a Connector.
	 * @author vlad
	 */
	private class Connector implements Drawable
	{
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
		
		public void paintDrawable(Graphics2D g2d)
		{
			AffineTransform saved = g2d.getTransform();
			
			g2d.setColor(Connector_COLOR);
			
			ConnectorShape.setLine(x0, y0, x1, y1);
			g2d.draw(ConnectorShape);
			
			g2d.setTransform(saved);
		}		
	}
}