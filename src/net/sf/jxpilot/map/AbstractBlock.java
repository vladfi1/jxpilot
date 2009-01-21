package net.sf.jxpilot.map;

import java.awt.Graphics2D;

public interface AbstractBlock {
	public BlockType getBlockType();
	public int getNum();
	public int getX();
	public int getY();
	
	public void render(int x, int y, Graphics2D g2d);
}
