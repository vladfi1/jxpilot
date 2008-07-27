package jxpilot;

import java.awt.geom.*;

import static jxpilot.MathFunctions.*;

public class Bullet extends BaseEntity implements java.io.Serializable
{
	private double LifeTime;
	private double LifeCount=0;
	private boolean FirstShot;
	
	public Bullet()
	{
		super();
	}
	
	Bullet(double radius, double LifeTime)
	{
		super(radius, 0);
		setBullet();
		this.LifeTime=LifeTime;
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	public void Move()
	{
		LifeCount += 1000/XPilotServer.FPS;
		if (LifeCount>= 1000*LifeTime) 
		{
			Destroy();
			//System.out.println("Destroyed");
		}
		if (FirstShot && LifeCount>2000/XPilotServer.FPS)
		{
			FirstShot = false;
		}
		
		super.Move();
		//System.out.println(LifeCount);
		//previous.setLocation(trueMod(center.getX(),MapW), trueMod(center.getY(),MapH));
		//moveBullet(Xspeed/XPilotServer.FPS, Yspeed/XPilotServer.FPS);
		//path.setLine(previous, center);
	}
	public void moveBullet(double dX, double dY)
	{
		//setLocation(center.getX()+dX,center.getY()-dY);
		super.MoveEntity(dX, dY);
	}
	
	public void setBullet()
	{
		//bullet.setFrame(center.getX()-radius, center.getY()-radius, 2*radius, 2*radius);
		super.setEntity();
	}
	
	public void Fire(double BulletSpeed, double Xspeed, double Yspeed, double angle, double x, double y)
	{
		LifeCount = 0;
		FirstShot=true;
		setLocation(x, y);
		this.Xspeed=Xspeed+BulletSpeed*degreeSin(angle);
		this.Yspeed=Yspeed+BulletSpeed*degreeCos(angle);
		active = true;
	}
	public void Fire(double BulletSpeed, double Xspeed, double Yspeed, double angle, Point2D p)
	{
		this.Fire(BulletSpeed, Xspeed, Yspeed, angle, p.getX(), p.getY());
	}
	
	public void Destroy()
	{
		LifeCount=0;
		active = false;
		super.Destroy();
	}
	
	public void setLocation(Point2D p)
	{
		super.setLocation(p);
	}
	
	public void setLocation(double x, double y)
	{
		super.setLocation(x, y);
	}
	
	public void setBullet(Bullet other)
	{
		active = other.active;
		super.setEntity(other);
	}
	
	//public Line2D getPath(){return path;}
	//public double getRadius(){return radius;}
	public boolean FirstShot(){return FirstShot;}
	//public Line2D getHitPath(){return hitPath;}
	//public void setHitPath(){hitPath.setLine(path);}
}