package net.sf.jxpilot.test;

import java.awt.*;

/**
 * Simple class that defines the basic elements of a shipshape and
 *  provides a way to turn ship into a string.
 * @author vlad
 *
 */

public class ShipShape
{
	public static String toString(Point p)
	{
		return p.x + "," + p.y;
	}
	
	
	public static final ShipShape defaultShip = 
		new ShipShape(new Point(-8,0), new Point(14,0), new Point(-8,8), new Point(-8,-8), new Point(14,0));
	
	private short version = 0x3200;
	private String extras = "\0";
	private Point engine, main_gun,
					left_light, right_light,
					main_rack;
	private Polygon shape;
	
	
	public ShipShape(Point engine, Point main_gun, Point left_light, Point right_light, Point main_rack)
	{
		this.engine = engine;
		this.main_gun = main_gun;
		this.left_light = left_light;
		this.right_light = right_light;
		this.main_rack = main_rack;
		shape = new Polygon();
		shape.addPoint(main_gun.x, main_gun.y);
		shape.addPoint(left_light.x, left_light.y);
		shape.addPoint(right_light.x, right_light.y);
	}
	
	/**
	 * @return A String representation of the ShipShape that is understood by xpilot
	 * 			and can be sent over the internet.
	 */
	public String toString()
	{
		return "(SH:)(EN: " + toString(engine) + 
				")(MG: " + toString(main_gun) +
				")(LL: " + toString(left_light) +
				")(RL: " + toString(right_light) +
				")(MR: " + toString(main_rack) + ")\0"
				+ extras;
	}
	
	
}
