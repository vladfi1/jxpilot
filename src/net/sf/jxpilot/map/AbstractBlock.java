package net.sf.jxpilot.map;

import java.awt.Graphics2D;

public interface AbstractBlock {
	/**
	 * The width and height of a Block.
	 */
	public static final int BLOCK_SIZE = 35;
	
	/**
	 * @see {@link BlockType}
	 * @return The corresponding {@code BlockType} constant.
	 */
	public BlockType getBlockType();
	/**
	 * @return The index of this block in the map data.
	 */
	public int getNum();
	/**
	 * @return The x coordinate of this block, in blocks.
	 */
	public int getX();
	/**
	 * @return The y coordinate of this block, in blocks.
	 */
	public int getY();
	
	/**
	 * Renders this block at the desired location.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param g2d The desired graphics context.
	 */
	public void render(int x, int y, Graphics2D g2d);
}
