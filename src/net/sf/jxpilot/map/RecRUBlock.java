package net.sf.jxpilot.map;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Color;

import java.awt.image.BufferedImage;

import net.sf.jgamelibrary.geom.AbstractPolygon2D;
import net.sf.jgamelibrary.geom.Polygon2D;
import net.sf.jgamelibrary.graphics.Accelerator;

public class RecRUBlock extends Block {
	public static final Color REC_RU_COLOR = Color.BLUE;
	
	private static final Polygon RU_POLYGON;
	public static final AbstractPolygon2D RU_POLYGON2D;
	private static final BufferedImage RU_IMAGE;
	static {
		int[] xpoints = {0, BLOCK_SIZE, 0};
		int[] ypoints = {0, 0, BLOCK_SIZE};
		RU_POLYGON = new Polygon(xpoints, ypoints, 3);
		RU_POLYGON2D = new Polygon2D(RU_POLYGON);
		RU_IMAGE = Accelerator.createCompatibleImage(BLOCK_SIZE, BLOCK_SIZE);
	}
	
	@SuppressWarnings("serial")
	public RecRUBlock(int num, int x, int y) {
		super(BlockType.REC_RU, num, x, y);

	}

	@Override
	public void render(int x, int y, Graphics2D g2d) {
		
	}

	@Override
	public void render(Graphics2D arg0) {
		// TODO Auto-generated method stub

	}

}
