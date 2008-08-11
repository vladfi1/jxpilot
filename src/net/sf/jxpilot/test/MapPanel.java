package net.sf.jxpilot.test;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class MapPanel extends JPanel
{
	private AffineTransform identity = new AffineTransform();
	private Color blockColor = Color.BLUE;
	private Color spaceColor = Color.BLACK;
	
	private Map map;
	private MapSetup setup;
	private MapObject[][] blocks;
	
	private int viewX=0, viewY=0;
	
	public MapPanel(Map map)
	{
		this.map = map;
		setup = map.getSetup();
		blocks = map.getBlocks();
	}
	
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		g.setColor(spaceColor);
		g.fillRect(0, 0, setup.getX() * MapObject.DEFAULT_WIDTH, setup.getY() * MapObject.DEFAULT_HEIGHT);
		
		for (MapObject[] array : blocks)
		{
			for (MapObject o : array)
			{
				paintBlock(g, o);
			}
		}
	}
	
	private void paintBlock(Graphics g, MapObject block)
	{
		if (block.getShape()==null) return;
		
		Graphics2D g2 = (Graphics2D) g;		
		
		g2.setTransform(identity);
		g2.translate(block.getX()*block.DEFAULT_WIDTH, (setup.getY()-block.getY()-1)*block.DEFAULT_HEIGHT);
		
		g2.setColor(blockColor);
		g2.draw(block.getShape());
	}
	
	
}
