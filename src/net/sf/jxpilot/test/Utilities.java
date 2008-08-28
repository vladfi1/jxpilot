package net.sf.jxpilot.test;

import java.awt.*;

/**
 * Class with various useful functions.
 * @author vlad
 *
 */
public class Utilities {
	public static int getUnsignedShort(short val)
	{
		return (int)((char)val);
	}
	public static short getUnsignedByte(byte val)
	{		
		return (short)(0xFF & (int)val);
	}
	
	/**
	 * @param heading The angle in 128 degree mode.
	 * @return The angle in radians.
	 */
	public static double getAngleFrom128(double heading)
	{
		return Math.PI/64.0 * heading;
	}
	
	/**
	 * Draws a String taking into account a flip of the Graphics object.
	 * @param g2d Graphics object on which to draw the String.
	 * @param s The String to be drawn.
	 * @param x Where the String is drawn.
	 * @param y Where the String is drawn.
	 */
	public static void drawFlippedString(Graphics2D g2d, String s, float x, float y)
	{
		g2d.scale(1, -1);
		g2d.drawString(s, x, -y);
		g2d.scale(1, -1);
	}
}
