package net.sf.jxpilot.test;

import java.awt.*;

public class Map
{
	private MapSetup setup;
	private MapObject[][] blocks;
	
	public Map(MapSetup setup)
	{
		this.setup = setup;
		
		blocks = new MapObject[setup.getX()][setup.getY()];
		
		for (int x = 0;x<setup.getX();x++)
		{
			for (int y = 0 ; y<setup.getY();y++)
			{
				blocks[x][y] = MapObject.getBlockShape(setup, setup.getBlock(x, y), x, y);
				blocks[x][y].setPosition(x, y);
			}
		}
	}
	
	public MapObject[][] getBlocks(){return blocks;}
	public MapSetup getSetup(){return setup;}
	
	private static interface blockHelper
	{
		public void processBlock(byte type, int x, int y);
	}
}
