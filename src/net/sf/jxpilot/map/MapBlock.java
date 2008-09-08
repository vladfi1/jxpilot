package net.sf.jxpilot.map;

import net.sf.jxpilot.graphics.*;
import java.awt.geom.*;
import java.awt.*;
import java.awt.image.*;

import static net.sf.jxpilot.map.BlockMapSetup.*;

/**
 * Represents a block of a BlockMap. Note that things that can change such as cannons are not represented by MapBlocks.
 * @author vlad
 */
public class MapBlock extends DrawableBlock implements java.io.Serializable, Drawable
{
	public static final int BLOCK_SIZE = 35;
	
	public final int num, x,y;
	
	private MapBlock(DrawableBlock block, int num, int x, int y)
	{
		super(block);
		this.num = num;
		this.x= x;
		this.y = y;
	}
	
	/**
	 * @return The block's index in the map data.
	 */
	public int getNum(){return num;}
	/**
	 * @return The x-position, in blocks.
	 */
	public int getX(){return x;}
	/**
	 * @return The y-position, in blocks.
	 */
	public int getY(){return y;}

	public void paintDrawable(Graphics2D g2d)
	{
		paintBlock(g2d, x, y);
	}
	
	/**
	 * This method uses optimized image-block drawing.
	 * @param g2d
	 * @param x Position in blocks.
	 * @param y Position in blocks.
	 */
	public void paintBlock(Graphics2D g2d, int x, int y)
	{
		if(!isDrawn) return;
		
		//g2d.translate(x*BLOCK_SIZE, y*BLOCK_SIZE);
		
		//super.paintDrawable(g2d);
		
		g2d.drawImage(super.image, x*BLOCK_SIZE, y*BLOCK_SIZE, null);
		
		//g2d.translate(-x*BLOCK_SIZE, -y*BLOCK_SIZE);
	}
	
	public static MapBlock getMapBlock(BlockMapSetup setup, int num)
	{
		byte block_type = setup.getMapData()[num];
		
		DrawableBlock block = DrawableBlock.getBlock(block_type);
		
		int x = setup.getX(num);
		int y = setup.getY(num);
		
		return new MapBlock(block, num, x, y);
		
	}
	
	public static int getScreenY(BlockMapSetup setup, int y)
	{
		return (setup.getY()-y-1);
	}
		
	public static String getBlockChar(byte block)
	{
		switch(block)
		{
		case SETUP_SPACE: return " ";
		case SETUP_FILLED: return "\0";
		case SETUP_FUEL: return "F";
		case SETUP_REC_RU: return "\\";
		case SETUP_REC_RD: return "/";
		case SETUP_REC_LU: return "/";
		case SETUP_REC_LD: return "\\";
		case SETUP_WORM_NORMAL:
		case SETUP_WORM_IN:
		case SETUP_WORM_OUT: return "W";
		}
		
		if (block>= SETUP_TREASURE && block <SETUP_TREASURE + 10)
		{
			return "O";
		}
		
		if (block >= SETUP_BASE_LOWEST && block <= SETUP_BASE_HIGHEST)
		{
			return "B";
		}
		
		//return String.valueOf(block);
		return "?";
	}
	
	public String toString()
	{
		return getBlockChar(super.block_type);
	}	
}