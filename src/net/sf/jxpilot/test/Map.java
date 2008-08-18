package net.sf.jxpilot.test;

import java.awt.*;

public class Map
{
	private MapSetup setup;
	private MapBlock[][] blocks;
	
	public Map(MapSetup setup)
	{
		this.setup = setup;
		
		blocks = new MapBlock[setup.getX()][setup.getY()];
		
		for (int x = 0;x<setup.getX();x++)
		{
			for (int y = 0 ; y<setup.getY();y++)
			{
				blocks[x][y] = MapBlock.getMapBlock(setup, setup.getBlock(x, y), x, y);
				blocks[x][y].setPosition(x, y);
			}
		}
	}
	
	public MapBlock[][] getBlocks(){return blocks;}
	public MapSetup getSetup(){return setup;}
	
	private static interface blockHelper
	{
		public void processBlock(byte type, int x, int y);
	}
	
	public String toString()
	{
		String temp = "";
		for (int y=setup.getY()-1;y>=0;y--)
		{
			for (int x = 0;x<setup.getY();x++)
			{
				temp += setup.getBlock(setup.getBlock(x, y));
			}
		}
		return temp;
	}
}
