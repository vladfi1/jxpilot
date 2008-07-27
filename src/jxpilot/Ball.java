package jxpilot;

import java.awt.geom.*;

import static XPilot.MathFunctions.*;

public class Ball extends BaseEntity implements java.io.Serializable
{
	
	Line2D connector;

	private transient double conLength;
	private transient double actualLength;
	private Ship attached;

	private transient double tight;
	private transient Point2D ShipCenter;
	private boolean connecting;

	private Rectangle2D box;
	private transient Ship thrown;
	
	//default constructor
	public Ball()
	{
		super();
		BallInit();
	}
	
	Ball(Ball other)
	{
		this();
		setBall(other);
	}
	
	Ball(double radius, double mass, Point2D p)
	{
		super(radius, mass, p);
		BallInit();
	}
	
	Ball(double radius, double length, Point2D p, double mass, double tight)
	{
		this(radius, mass, p);
		conLength = length;
		attached=null;
		this.tight=tight;
		
		double x = base.getX();
		double y = base.getY();
		box.setFrame(x-2*radius,y-2*radius,4*radius,4*radius);
		
		setBall();
	}
	
	private void BallInit()
	{
		connector = new Line2D.Double();
		ShipCenter = new Point2D.Double();
		connecting = false;
		box = new Rectangle2D.Double();
	}
	
	public void setBall()
	{
		super.setEntity();
		setConnector();
	}
	
	public void setConnector()
	{
		if (attached!=null)
		{
			
			ShipCenter.setLocation(attached.getCenter());
			
			/*
			double dX = (center.getX()-ShipCenter.getX());
			double dW = 0;
			
			if (dX>MapW/2) {dW=MapW;}
			else if (dX<-MapW/2) {dW=-MapW;}
			
			double dY = (center.getY()-ShipCenter.getY());
			double dH = 0;
			
			if (dY>MapH/2) {dH=MapH;}
			else if (dY<-MapH/2) {dH=-MapH;}
			
			ShipCenter.setLocation(ShipCenter.getX()+dW, ShipCenter.getY()+dH);
			*/
			//System.out.println("setting connector");
		}
		else
		{
			//if (ShipCenter==null) System.out.println("null");
			ShipCenter.setLocation(this.center);			
			
		}
		connector.setLine(this.center, this.ShipCenter);
		WrapLineClose(connector, MapW, MapH);
		actualLength = LinLength(connector);
	}
	
	public void setBall(Ball other)
	{
		super.setEntity(other);
		
		box.setFrame(other.box);
		connector.setLine(other.connector);
		connecting = other.connecting;
	}
	
	public void moveBall(double dX, double dY)
	{
		super.MoveEntity(dX, dY);
		setBall();
	}
	
	/*
	public void WrapBall(double dX, double dY)
	{
		center.setLocation(center.getX()+dX, center.getY()-dY);
		setBall();
	}
	*/
	
	public void Force(double XForce, double YForce)
	{
		Xspeed += XForce/mass;
		Yspeed += YForce/mass;
	}

	public void setLocation(Point2D p)
	{
		super.setLocation(p);
		setBall();
	}
	
	public void setLocation(double x, double y)
	{
		center.setLocation(x, y);
		setBall();
	}
	
	public void ConnectorForce()
	{
		setConnector();
		
		//System.out.println(attached==null);
		if (attached != null && !connecting)
		{
			
			//find center of gravity
			
			//double X = (center.getX()*mass+attached.getRealCenter().getX()*attached.getMass())/(mass+attached.getMass());
			//double Y = (center.getY()*mass+attached.getRealCenter().getY()*attached.getMass())/(mass+attached.getMass());
			//
			//this.setLocation(((X+(center.getX()-X)*conLength/actualLength))/*+(center.getX()))/2*/, 
			//		(((Y+(center.getY()-Y)*conLength/actualLength)))/*+(center.getY()))/2*/);
			//
			//attached.setLocation(((X+(attached.getRealCenter().getX()-X)*conLength/actualLength))/*+(attached.getCenter().getX()))/2*/, 
			//		((Y+(attached.getRealCenter().getY()-Y)*conLength/actualLength))/*+(attached.getCenter().getY()))/2*/);

			//Victor's physics----turns out to be the same as NG physics
			double DX = connector.getX2()-connector.getX1();
			double DY = connector.getY1()-connector.getY2();
			
			double dif = (conLength-actualLength)/conLength;
			
			//connector breakage
			if (Math.abs(dif)>0.5 && XPilotServer.breakable)
			{
				disConnect();
				return;
			}
			
			//System.out.println(dif);
			double XForce = tight*dif*(DX/actualLength)/FPS;
			double YForce = tight*dif*(DY/actualLength)/FPS;
			//System.out.println(conLength-actualLength);

			
			double DXspeed = attached.getXspeed()-this.Xspeed;
			double DYspeed = attached.getYspeed()-this.Yspeed;
			
			double CompressionSpeed = (DX*DXspeed+DY*DYspeed)/(actualLength);
			
			double FrictionCoefficient = 1;
			
			double BallFrictionForceX = FrictionCoefficient * CompressionSpeed * (DX/actualLength)/FPS;
			double BallFrictionForceY = FrictionCoefficient * CompressionSpeed * (DY/actualLength)/FPS;
			
			this.Force(-XForce, -YForce);
			attached.Force(XForce, YForce);
			Force(BallFrictionForceX, BallFrictionForceY);
			attached.Force(-BallFrictionForceX, -BallFrictionForceY);
			
			/*
			//Xpilot NG ball physics
			double Dx = connector.getX2()-connector.getX1();
			double Dy = connector.getY1()-connector.getY2();
			
			Dx /= actualLength;
			Dy /= actualLength;
			
			double ratio = 1- actualLength/(conLength);
			
			double force = tight*ratio;
			
			double FrictionCoefficient = 1;
			double friction = -FrictionCoefficient*
			((attached.getXspeed()-Xspeed)*Dx+(attached.getYspeed()-Yspeed)*Dy);
			
			double accell = (force+friction)/FPS;
			double XForce = Dx*accell;
			double YForce = Dy*accell;
			
			attached.Force(XForce, YForce);
			Force(-XForce, -YForce);
			*/
		}
	}
	
	public Point2D getCenter()
	{
		return center;
	}
	
	public Line2D getConnector(){return connector;}
	public double getConLength(){return conLength;}
	public Line2D getPath(){return path;}
	public boolean isConnecting(){return connecting;}
	public Rectangle2D getBox(){return box;}
	public Ship thrown(){return thrown;}
	public Ship attached()
	{
		if (connecting)
		{
			return null;
		}
		return attached;
	}
	
	public void Connect(Ship s)
	{

		if (!this.isAttached() && !(s.getTeam()==team && !active))
		{
			
			//finds correct path closest to ball
			s.setHitPath();
			
			double dX = (path.getX1()-s.getPath().getX1());
			double dW = 0;
		
			if (dX>MapW/2) {dW=MapW;}
				else if (dX<-MapW/2) {dW=-MapW;}
		
			double dY = (path.getY1()-s.getPath().getY1());
			double dH = 0;
		
			if (dY>MapH/2) {dH=MapH;}
				else if (dY<-MapH/2) {dH=-MapH;}
		
			translateLine(s.getHitPath(), dW, -dH);
			
			if (LinDist(path, s.getHitPath())<=conLength)
			{
				attached = s;
				connecting =true;
				if (path.ptSegDist(s.getHitPath().getP2())>=conLength)
				{
					thrown = s;
					active=true;
					connecting = false;
					
					setBall();
				}
			}
			else
			{
				connecting = false;
			}
		}
	}
	
	public void disConnect()
	{
		//attached.setAttached(false);
		connecting = false;
		attached=null;
	}
	
	public boolean isAttached()
	{
		//System.out.println(attached==null);
		return !(attached==null || connecting);
	}
	public boolean isAttachedTo(Ship s){return (s==attached);}
	
	public void Destroy()
	{
		//setLocation(base);
		disConnect();
		thrown=null;
		active = false;
		setBall();
		super.Destroy();
		//this.setLocation(this.base);
	}
	
	public boolean BoxCollision(BaseEntity other)
	{
		return (other.path.ptSegDist(base)<other.radius+box.getWidth()/2);
	}
}