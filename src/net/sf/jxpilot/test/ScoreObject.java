package net.sf.jxpilot.test;

import java.awt.*;
import java.awt.geom.*;

public class ScoreObject implements ExtendedDrawable<ScoreObject>
{
	private static final Color SCORE_OBJECT_COLOR = Color.WHITE;
	
	private short score;
	private short x,y;
	private String message;
	
	public ScoreObject setScoreObject(short score, short x, short y, String message)
	{
		this.score = score;
		this.x = x;
		this.y = y;
		this.message= message;
		return this;
	}
	
	public void set(ScoreObject other)
	{
		setScoreObject(other.score, other.x, other.y, other.message);
	}
	
	public ScoreObject getNewInstance()
	{
		return new ScoreObject();
	}
	
	public void paintDrawable(Graphics2D g2d)
	{
		AffineTransform saved = g2d.getTransform();
		
		g2d.setColor(SCORE_OBJECT_COLOR);
		g2d.translate(x*MapBlock.BLOCK_SIZE, y*MapBlock.BLOCK_SIZE);
		
		Utilities.drawFlippedString(g2d, String.valueOf(score), x, y);
		FontMetrics fm = g2d.getFontMetrics();
		Rectangle2D bounds = fm.getStringBounds(message, g2d);
		
		Utilities.drawFlippedString(g2d, message, (float)(x-bounds.getWidth()/2.0), (float)(y-bounds.getHeight()/2.0));
		
		g2d.setTransform(saved);
	}
}