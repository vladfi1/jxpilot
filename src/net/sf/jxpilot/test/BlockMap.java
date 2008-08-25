package net.sf.jxpilot.test;

/**
 * Represents a block-based xpilot map.
 * @author vlad
 *
 */
public class BlockMap implements java.io.Serializable
{
	private MapSetup setup;
	private MapBlock[][] blocks;
	
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
	}
	
	public MapBlock[][] getBlocks(){return blocks;}
	public MapSetup getSetup(){return setup;}
	
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