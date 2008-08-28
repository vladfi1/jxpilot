package net.sf.jxpilot.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

/**
 * Simple class for storing a shot.
 * @author vlad
 */
public class FastShot extends AbstractDebris<FastShot>
{
	//drawing info
	public static final int SHOT_RADIUS = 2;
	private static final Color TEAM_COLOR = Color.BLUE;
	private static final Color ENEMY_COLOR = Color.WHITE;
	private static final Ellipse2D shotShape = 
		new Ellipse2D.Float(-SHOT_RADIUS,-SHOT_RADIUS,2*SHOT_RADIUS,2*SHOT_RADIUS);
	
	private void setFastShot()
	{
		super.COLOR = ENEMY_COLOR;
		super.debrisShape = shotShape;
	}
	
	public FastShot()
	{
		setFastShot();
	}
	
	public FastShot(AbstractClient client){super(client);}
	
	public static FastShot createHolder()
	{
		return new FastShot();
	}
	
	/**
	 * Generates a new Shot, with a reference to the same client.
	 */
	@Override
	public FastShot getNewInstance()
	{
		FastShot temp = new FastShot();
		temp.setClient(this);
		return temp;
	}
	
	public void setDrawable(FastShot other)
	{
		super.setDrawable(other);
	}
}