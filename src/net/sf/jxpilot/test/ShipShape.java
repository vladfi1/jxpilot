package net.sf.jxpilot.test;

import java.awt.*;
import java.util.regex.*;
/**
 * Simple class that defines the basic elements of a shipshape and
 *  provides a way to turn ship into a string.
 * @author vlad
 * 
 */

public class ShipShape
{
	
	public static final String intRegex = "\\-?\\d+";
	private static final Pattern intPattern = Pattern.compile(intRegex);
	
	public static final String POINT_SEPARATOR = ",";
	public static final String pointRegex = intRegex + POINT_SEPARATOR + intRegex;
	private static final Pattern pointPattern = Pattern.compile(pointRegex);
	
	public static final String parenthesesRegex = "\\([^\\(\\)]*\\)";
	private static final Pattern parenthesesPattern = Pattern.compile(parenthesesRegex);
	
	public static final String markerRegex = "[A-Z]{2}";
	private static final Pattern markerPattern = Pattern.compile(markerRegex);
	
	
	private static final String SHIP_MARKER = "SH";
	private static final String ENGINE_MARKER = "EN";
	private static final String MAIN_GUN_MARKER = "MG";
	private static final String LEFT_LIGHT_MARKER = "LL";
	private static final String RIGHT_LIGHT_MARKER = "RL";
	private static final String MAIN_RACK_MARKER = "MR";
	
	public static String toString(Point p)
	{
		return p.x + POINT_SEPARATOR + p.y;
	}
	
	/**
	 * @param str The String to be parsed.
	 * @return The first point (x,y) if the String contains the pattern int,int
	 * 			null otherwise.
	 */
	public static Point parsePoint(String str)
	{
		Matcher matcher = pointPattern.matcher(str);
		
		if (!matcher.find()) return null;
		
		String point = matcher.group();
		
		//System.out.println("Found point group: "+point);
		
		matcher = intPattern.matcher(point);
		
		if (!matcher.find())
		{
			System.out.println("No ints found in point parse!");
			return null;
		}
		int x = Integer.parseInt(matcher.group());
		
		if (!matcher.find()) 
		{
			System.out.println("No ints found in point parse!");
			return null;		
		}
		int y = Integer.parseInt(matcher.group());
	
		return new Point(x, y);
	}
	
	public static final ShipShape defaultShip = 
		new ShipShape(new Point(-8,0), new Point(14,0), new Point(-8,8), new Point(-8,-8), new Point(14,0));
	
	private short version = 0x3200;
	private Polygon shape;
	private Point engine, main_gun,
					left_light, right_light,
					main_rack;
	private String extras = "\0";
	
	private ShipShape()
	{
		shape = new Polygon();
	}
	
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
	
	public Polygon getShape(){return shape;}
	
	/**
	 * @return A String representation of the ShipShape that is understood by xpilot
	 * 			and can be sent over the internet.
	 */
	public String toString()
	{
		String temp= "(SH:";
		for(int i =0;i<shape.npoints;i++)
		{
			temp += " " + shape.xpoints[i] + POINT_SEPARATOR + shape.ypoints[i];
		}
		
		temp += ")(EN: " + toString(engine) + 
				")(MG: " + toString(main_gun) +
				")(LL: " + toString(left_light) +
				")(RL: " + toString(right_light) +
				")(MR: " + toString(main_rack) + ")\0"
				+ extras;
		
		return temp;
	}
	
	public static ShipShape parseShip(String shipStr, String extension)
	{
		ShipShape ship=new ShipShape();
		
		Matcher parenthesesMatcher = parenthesesPattern.matcher(shipStr);
		
		while(parenthesesMatcher.find())
		{
			
			
			String parentheses = parenthesesMatcher.group();
			//System.out.println("Found parentheses group: "+ parentheses);
			
			
			Matcher markerMatcher = markerPattern.matcher(parentheses);
			markerMatcher.find();
			String marker = markerMatcher.group();
			
			if (marker.compareTo(SHIP_MARKER)==0)
			{
				Matcher pointMatcher = pointPattern.matcher(parentheses);
				
				while(pointMatcher.find())
				{
					
					String pointStr = pointMatcher.group();
					//System.out.println("Found point group: "+pointStr);
					
					Point p = parsePoint(pointStr);
					
					ship.shape.addPoint(p.x, p.y);
				}
				continue;
			}
			
			if (marker.compareTo(ENGINE_MARKER)==0)
			{
				Matcher pointMatcher = pointPattern.matcher(parentheses);
				pointMatcher.find();
				ship.engine = parsePoint(pointMatcher.group());
				continue;
			}
			if (marker.compareTo(MAIN_GUN_MARKER)==0)
			{
				Matcher pointMatcher = pointPattern.matcher(parentheses);
				pointMatcher.find();
				ship.main_gun = parsePoint(pointMatcher.group());
				continue;
			}
			if (marker.compareTo(LEFT_LIGHT_MARKER)==0)
			{
				Matcher pointMatcher = pointPattern.matcher(parentheses);
				pointMatcher.find();
				ship.left_light = parsePoint(pointMatcher.group());
				continue;
			}
			if (marker.compareTo(RIGHT_LIGHT_MARKER)==0)
			{
				Matcher pointMatcher = pointPattern.matcher(parentheses);
				pointMatcher.find();
				
				ship.right_light = parsePoint(pointMatcher.group());
				continue;
			}
			if (marker.compareTo(MAIN_RACK_MARKER)==0)
			{
				Matcher pointMatcher = pointPattern.matcher(parentheses);
				pointMatcher.find();
				
				ship.main_rack = parsePoint(pointMatcher.group());
				continue;
			}
			
		}
		
		ship.extras = extension;
		
		return ship;
		
	}
	
}