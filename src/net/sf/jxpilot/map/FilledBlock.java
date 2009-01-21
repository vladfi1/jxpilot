package net.sf.jxpilot.map;

import java.awt.Graphics2D;
import java.awt.Color;

import net.sf.jgamelibrary.graphics.GfxUtil;

public class FilledBlock extends Block {
	public static final Color FILLED_COLOR = Color.BLUE;
	
	public FilledBlock(BlockType type, int num, int x, int y) {
		super(type, num, x, y);
	}

	@Override
	public void render(Graphics2D g2d) {
		GfxUtil.fillRect(x, y+BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, g2d);
	}

}
