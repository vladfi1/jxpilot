package jxpilot;

import static jxpilot.MathFunctions.*;
import java.awt.geom.*;

public class BaseEntity implements java.io.Serializable
{
	protected Ellipse2D circle;
	protected double radius;
	protected Point2D center;
	protected double mass;
	protected double Xspeed=0;
	protected double Yspeed=0;
	protected Line2D path;
	protected transient Point2D previous;
	protected Line2D hitPath;
	protected Point2D base;
	protected transient Line2D wall1, wall2;
	protected Rectangle2D hitBox;
	protected transient WallHit hit;
	protected transient WallHolder holder;
	protected boolean inUse;
	
	protected int team;
	
	protected static double MapW, MapH;
	protected boolean active=false;
	
	public static void setWidth(double w)
	{MapW=w;}
	public static void setHeight(double h)
	{MapH=h;}
	
	protected static int FPS;
	public static void setFPS(int F) {FPS = F;}
	
	//default constructor
	public BaseEntity()
	{
		center = new Point2D.Double();
		circle = new Ellipse2D.Double();
		previous = new Point2D.Double();
		path = new Line2D.Double();
		hitPath = new Line2D.Double();
		base = new Point2D.Double();
		wall1 = new Line2D.Double();
		wall2 = new Line2D.Double();
		hitBox = new Rectangle2D.Double();
		hit = new WallHit();
		holder = new WallHolder();
		inUse = false;
	}
	
	BaseEntity(double radius, double mass)
	{
		this();
		this.radius=radius;
		this.mass = mass;
	}
	
	BaseEntity(double radius, double mass, Point2D p)
	{
		this(radius, mass);
		base.setLocation(p);
		this.setLocation2(base);
	}
	
	BaseEntity(BaseEntity other)
	{
		this();
		setEntity(other);
	}
	
	public static BaseEntity copy(BaseEntity other)
	{
		if (other!=null) return new BaseEntity(other);
		else return null;
	}
	
	public void setEntity()
	{
		circle.setFrame(center.getX()-radius, center.getY()-radius, 2*radius, 2*radius);
	}
	
	public void setEntity(BaseEntity other)
	{
		radius=other.radius;
		setLocation(other.center);
		team=other.team;
		path.setLine(other.path);
		setEntity();
	}
	
	public void setLocation2(Point2D p)
	{
		this.center.setLocation(p);
		setEntity();
	}
	
	public void setLocation(Point2D p)
	{
		this.center.setLocation(p);
		setEntity();
	}
	public void setLocation(double x, double y)
	{
		this.center.setLocation(x, y);
		setEntity();
	}
	public void setPath()
	{
		path.setLine(previous, center);
	}
	public void setHitBox()
	{
		hitBox.setFrameFromDiagonal(path.getP1(), path.getP2());
		hitBox.setFrame(hitBox.getMinX()-radius, hitBox.getMinY()-radius,
				hitBox.getWidth()+2*radius, hitBox.getHeight()+2*radius);
	}
	
	public Ellipse2D getCircle(){return circle;}
	public double getRadius(){return radius;}
	public Point2D getCenter(){return center;}
	public double getXspeed(){return Xspeed;}
	public double getYspeed(){return Yspeed;}
	public double getMass(){return mass;}
	public Line2D getPath(){return path;}
	public Line2D getHitPath(){return hitPath;}
	public void setHitPath(){hitPath.setLine(path);}
	public Point2D getBase(){return base;}
	public void setBase(Point2D p){base=p;}
	public void setTeam(int t){team =t;}
	public int getTeam(){return team;}
	public boolean isActive(){return active;}
	public boolean inUse(){return inUse;}
	public void setUse(boolean b){inUse = b;}
	
	public void Force(double XForce, double YForce) 
	{
		Xspeed += XForce/mass;
		Yspeed += YForce/mass;
	}
	
	//for regular movement
	public void MoveEntity(double dX, double dY)
	{
		setLocation(center.getX()+dX, center.getY()-dY);
	}
	
	public void Move()
	{
		//previous.setLocation(center);
		previous.setLocation(trueMod(center.getX(),MapW), trueMod(center.getY(),MapH));
		MoveEntity(Xspeed/FPS, Yspeed/FPS);
		setPath();
		
		setHitBox();
		
		//wraps around border
		wrapBorder();
	}
	
	public void wrapBorder()
	{
		setLocation(trueMod(center.getX(), MapW), trueMod(center.getY(), MapH));
	}

	public void Destroy()
	{
		Xspeed=0;
		Yspeed=0;
		if (base!= null)
		{
			this.setLocation(base);
			previous.setLocation(base);
			path.setLine(base, base);
		}//else System.out.println("No Base Error");
	}
	
	public boolean Collision(BaseEntity other)
	{
		setHitPath();
		other.setHitPath();
		
		/*
		double dX = other.getHitPath().getX2()-other.getHitPath().getX1();
		double dY = other.getHitPath().getY1()-other.getHitPath().getY2();
		
		
		//System.out.println("before: "+(hitPath.getY2()-hitPath.getY1()));
		hitPath.setLine(hitPath.getP1(), translatePoint(hitPath.getP2(), -dX, -dY));
		//System.out.println("after: "+(hitPath.getY2()-hitPath.getY1()));
		*/
		
		VectorSubtract(hitPath, other.hitPath);
		
		if (hitPath.ptSegDist(other.hitPath.getP1())<
				radius+other.radius)
		{
			//System.out.println("other: " + other.hitPath.getY1());
			//System.out.println("this: " + this.hitPath.getY1());
			//System.out.println("after: "+(hitPath.getY2()-hitPath.getY1()));
			return true;
		}
		
		wrapLine(hitPath, MapW, MapH);
		
		if (hitPath.ptSegDist(other.hitPath.getP1())<
				radius+other.radius)
		{return true;}
		
		return false;
	}
	
	public WallHit WallCollision(Line2D wall)
	{
		holder.setWall(wall, radius);
		
		if (hitBox.intersectsLine(wall))
		{
			hit = holder.pathHit(hit, path, radius);
		}
		else
		{
			hit.setHit(WallHit.NO_HIT, -1, null, null);
		}
		return hit;
	}
	
	public void setWalls(Line2D wall)
	{
		double dX = radius*(wall.getY2()-wall.getY1())/LinLength(wall);
		double dY = radius*(wall.getX2()-wall.getX1())/LinLength(wall);
		
		wall1.setLine(wall);
		translateLine(wall1, dX, dY);
		wall2.setLine(wall);
		translateLine(wall2,-dX,-dY);
	}
	
	public void Bounce(WallHit w, double friction)
	{
		if (w.hit_type() == WallHit.NO_HIT)
		{return;}
		else
		{	
			Line2D wall = w.wall;
			
			/*
			switch(hit_type)
				{
				case WALL1_HIT: wall = wall1;
				break;
				case WALL2_HIT: wall = wall2;
				break;
				default: wall = new Line2D.Double();
				}
			
			if (hit_type==POINT_HIT)
			{
				PointBounce(wall, friction);
				return;
			}
			*/
			
			double Lx = wall.getX2()-wall.getX1();
			double Ly = wall.getY1()-wall.getY2();
		
		//Line2D p = s.getPath();
		
			double Sx = FPS*(path.getX2()-path.getX1());
			double Sy = FPS*(path.getY1()-path.getY2());
		
			double Px = Ly;
			double Py = -Lx;	
		
			double length = LinLength(wall);
			double PLength = (Sx*Px+Sy*Py)/(length);
		
			double XForce = PLength*Px/length;
			double YForce = PLength*Py/length;
		
			XForce *= -(2)*mass;
			YForce *= -(2)*mass;
		
			Force(XForce, YForce);
			Xspeed *= friction/100;
			Yspeed *= friction/100;
			
			setLocation(reflectPointLine(path.getP2(), wall));
			
			previous.setLocation(w.hit);
			previous.setLocation(trueMod(previous.getX(),MapW), trueMod(previous.getY(),MapH));
			
			setPath();
			
			WrapLineClose(path, MapW, MapH);
			
			setLocation(path.getP2());
			
			setHitBox();
		}
	}
	
	public void PointBounce(Line2D wall, double friction)
	{
		Xspeed *= -friction/100;
		Yspeed *= -friction/100;
		
		setLocation(path.getP1());
	}
}