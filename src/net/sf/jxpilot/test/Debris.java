package net.sf.jxpilot.test;

import java.awt.Color;
import java.awt.geom.Ellipse2D;

public class Debris extends AbstractDebris<Debris>
{
	//drawing info
	public static final float SHOT_RADIUS = (float)0.5;
	private static final Color SPARK_COLOR = Color.RED;
	private static final Ellipse2D shotShape = 
		new Ellipse2D.Float(-SHOT_RADIUS,-SHOT_RADIUS,2*SHOT_RADIUS,2*SHOT_RADIUS);
	
	private void setDebris()
	{
		super.COLOR = SPARK_COLOR;
		super.debrisShape = shotShape;
	}
	
	public Debris()
	{
		setDebris();
	}
	
	public Debris(AbstractClient client){super(client);}
	
	public static Debris createHolder()
	{
		return new Debris();
	}
	
	/**
	 * Generates a new Shot, with a reference to the same client.
	 */
	@Override
	public Debris getNewInstance()
	{
		Debris temp = new Debris();
		temp.setClient(this);
		return temp;
	}
	
	public void setDrawable(Debris other)
	{
		super.setDrawable(other);
	}
}

