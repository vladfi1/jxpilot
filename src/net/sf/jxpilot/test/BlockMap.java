package net.sf.jxpilot.test;

import static net.sf.jxpilot.test.MapBlock.BLOCK_SIZE;

/**
 * Represents a block-based xpilot map.
 * @author vlad
 */
public class BlockMap implements java.io.Serializable
{
	private MapSetup setup;
	private MapBlock[][] blocks;
	private int width, height;
	
	public BlockMap(MapSetup setup)
	{
		this.setup = setup;
		
		blocks = new MapBlock[setup.getX()][setup.getY()];
		
		for (int x = 0;x<setup.getX();x++)
		{
			for (int y = 0 ; y<setup.getY();y++)
			{
				blocks[x][y] = MapBlock.getMapBlock(setup.getBlock(x, y), x, y);
				//blocks[x][y].setPosition(x, y);
			}
		}
		
		width = setup.getX() * BLOCK_SIZE;
		height = setup.getY() * BLOCK_SIZE;
	}
	
	public MapBlock[][] getBlocks(){return blocks;}
	public MapSetup getSetup(){return setup;}
	public MapBlock getBlock(short x, short y){return blocks[x][y];}
	public int getWidth(){return width;}
	public int getHeight(){return height;}
	
	public String toString()
	{
		String temp = "";
		for (int y=setup.getY()-1;y>=0;y--)
		{
			for (int x = 0;x<setup.getY();x++)
			{
				temp += blocks[x][y];
			}
			temp+="\n";
		}
		return temp;
	}
}