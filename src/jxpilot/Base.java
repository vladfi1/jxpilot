package jxpilot;

import java.awt.geom.*;
import static XPilot.MathFunctions.*;

public class Base implements java.io.Serializable
{
	private static double radius;
	
	private String name;
	private Point2D base;
	private double angle;
	private int team;
	private boolean taken= false;
	private Ship ship;
	private Line2D line;
	
	public Base()
	{
		base = new Point2D.Double();
		angle = 0;
		team = 0;
		name = null;
		line = new Line2D.Double();
	}
	
	public Base(Point2D p)
	{
		this();
		base.setLocation(p);
	}
	
	public Base(Point2D p, double a)
	{
		this(p);
		angle = a;
	}
	
	public Base(Point2D p, double a, int team)
	{
		this(p,a);
		this.team = team;
	}
	
	public void setBase(Base other)
	{
		setName(other.name);
		setBase(other.base);
		setAngle(other.angle);
		setTeam(other.team);
	}
	
	public void setRealBase(Point2D p, double radius)
	{
		p.setLocation(base);
		translatePointAngle(p, radius+1, angle);
	}
	
	public void setLine()
	{
		line.setLine(base, base);
		line.setLine(translatePointAngle(line.getP1(), radius,angle+90),
				translatePointAngle(line.getP2(), radius, angle-90));
	}
	
	public String getName(){return name;}
	public Point2D getBase(){return base;}
	public double getAngle(){return angle;}
	public int getTeam(){return team;}
	public Ship getShip(){return ship;}
	public Line2D getLine(){return line;}
	public boolean isTaken(){return ship!=null;}
	
	public void setShip(Ship s)
	{
		ship =s;
		name = s.name;
		taken = true;
	}
	
	public boolean take(Ship s)
	{
		if (ship == null)
		{
			setShip(s);
			taken = true;
			return true;
		}
		
		return false;
	}
	
	public void leave()
	{
		ship = null;
		taken = false;
		name = null;
	}
	
	public void setName(String n){name =n;}
	public void setBase(Point2D p){base.setLocation(p);}
	public void setAngle(double a){angle = a;}
	public void setTeam(int t){team = t;}
	
	public static void setRadius(double r)
	{
		radius = r;
	}
}
