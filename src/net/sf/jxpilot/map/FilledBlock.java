package net.sf.jxpilot.map;

import java.awt.Graphics2D;
import java.awt.Color;

import net.sf.jgamelibrary.graphics.GfxUtil;

public class FilledBlock extends Block {
	public static final Color FILLED_COLOR = Color.BLUE;
	
	public FilledBlock(int num, int x, int y) {
		super(BlockType.FILLED, num, x, y);
	}

	@Override
	public void render(int x, int y, Graphics2D g2d) {
		GfxUtil.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE, g2d);
	}
}
