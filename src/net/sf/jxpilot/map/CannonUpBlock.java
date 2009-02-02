package net.sf.jxpilot.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import net.sf.jgamelibrary.geom.Polygon2D;
import net.sf.jgamelibrary.geom.Polygon2DAdaptor;
import net.sf.jgamelibrary.geom.Vector2D;
import net.sf.jgamelibrary.graphics.Accelerator;
import net.sf.jgamelibrary.graphics.GfxUtil;
import net.sf.jxpilot.game.CannonHolder;

public class CannonUpBlock extends CannonHolder implements AbstractBlock {
	public static final Color CANNON_UP_COLOR = Color.WHITE;
	
	private static final Polygon2D CANNON_UP_POLYGON2D;
	public static final Polygon2DAdaptor getCannonUpPolygon2D() {return CANNON_UP_POLYGON2D;}
	
	private static final BufferedImage CANNON_IMAGE;
	
	static {
		CANNON_UP_POLYGON2D = new Polygon2D(new Vector2D[]{
				new Vector2D(0,0),
				new Vector2D(BLOCK_SIZE, 0),
				new Vector2D(BLOCK_SIZE/2.0, BLOCK_SIZE/4.0)
			});
		
		CANNON_IMAGE = Accelerator.createCompatibleImage(BLOCK_SIZE, BLOCK_SIZE);
		Graphics2D g2d = CANNON_IMAGE.createGraphics();
		g2d.setTransform(BLOCK_TRANSFORM);
		CANNON_UP_POLYGON2D.draw(g2d);
		g2d.dispose();
	}
	
	public final int x, y;
	
	public CannonUpBlock(int num, int x, int y) {
		this.num = num;
		this.x = x;
		this.y = y;
	}
	
	@Override
	public BlockType getBlockType() {return BlockType.CANNON_UP;}

	@Override
	public int getX() {return x;}

	@Override
	public int getY() {return y;}

	@Override
	public void render(int x, int y, Graphics2D g2d) {
		GfxUtil.drawImage(CANNON_IMAGE, x, y, g2d);
	}
}
