package net.sf.jxpilot.test;

import java.awt.*;

public interface Drawable 
{
	/**
	 * Note that this method may not change the transform of g2d.
	 * @param g2d The graphics object to draw on.
	 */
	public void paintDrawable(Graphics2D g2d);
}
