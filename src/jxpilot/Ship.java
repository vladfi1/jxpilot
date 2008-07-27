package jxpilot;

import java.awt.geom.*;

import static jxpilot.MathFunctions.*;

public class Ship extends BaseEntity implements java.io.Serializable
{
	private Point2D[] points;
	private Line2D[] lines;
	private Point2D fire;//used to fire bullets from
	private double angle;//measured clockwise from a vertical line
	private double bulletRadius;
	private Bullet[] bullets;
	private double CoolDown;
	private double CoolTime=-1;
	private double shielded=-1;
	private double ShieldTime;
	private double deadTime=-1;
	private double spawnTime;
	private transient Base base;
	private transient Base previous;
	
	private boolean paused = false;
	
	public String name="";
	private int score=0;
	
	private static int startLives;
	
	public static void setStartLives(int lives)
	{startLives = lives;}
	
	private int lives;
	
	public Ship()
	{
		super();
		lives = startLives;
	}
	
	//default constructor for registers
	Ship(int numBullets)
	{
		this();
		points = new Point2D[3];
		for (int i =0; i<3;i++)
		{points[i]=new Point2D.Double(0,0);}
		lines = new Line2D[3];
		for (int i =0; i<3;i++)
		{lines[i]=new Line2D.Double();}
		
		bullets = new Bullet[numBullets];
		
		if (bullets.length > 0)
		{
		for (int i=0;i<bullets.length;i++)
		{bullets[i] = new Bullet(bulletRadius, XPilotServer.BULLETLIFE);}
		}
		
	}
	
	Ship(double aRadius, double nAngle, double aMass, int numBullets, double abulletRadius, double aTime, double ShieldTime, double spawnTime)
	{
		this(numBullets);
		
		angle = nAngle;
		radius = aRadius;
		mass = aMass;
		fire = new Point2D.Double();
		setShip();
		
		bulletRadius = abulletRadius;
		bullets = new Bullet[numBullets];
		
		if (bullets.length > 0)
		{
		for (int i=0;i<bullets.length;i++)
		{bullets[i] = new Bullet(bulletRadius, XPilotServer.BULLETLIFE);}
		}
		CoolDown = aTime;
		this.ShieldTime = ShieldTime;
		this.spawnTime=spawnTime;
		
		base = new Base();
		previous = base;
	}
	
	public static Ship copy(Ship other)
	{
		if (other!=null) return new Ship(other);
		else return null;
	}
	
	Ship(Ship other)
	{
		this(other.radius, other.angle, other.mass, other.bullets.length, other.bulletRadius, other.CoolTime, other.ShieldTime, other.deadTime);
		setShip(other);
	}
	
	//creates a copy of the ship
	public void setShip(Ship other)
	{

		super.setEntity(other);
		
		angle = other.angle;
		name = other.name;
		score = other.score;
			
		if (bullets.length==other.bullets.length)
		{
			for (int i = 0; i< other.bullets.length; i++)
			{
				bullets[i].setBullet(other.bullets[i]);
			}
		}else{System.out.println("setBullet error in setShip");}
		
		shielded = other.shielded;
		this.setUse(other.inUse());
		
		//setShip();
	}
	
	//given the angle and center location, sets other lines
	public void setShip()
	{
		//shield
		super.setEntity();
		
		//sets lines and points
		for (int i = 0; i<3;i++)
		{
		double newAngle = angle+((i+1)%3-1)*135;
		points[i].setLocation(center.getX()+radius*degreeSin(newAngle),
				center.getY()-radius*degreeCos(newAngle));
		}
		for (int i=0;i<3;i++)
		{lines[i].setLine(points[i%3], points[(i+1)%3]);}
		if (fire != null)
		{
			fire.setLocation(center);
			translatePoint(fire, radius*degreeSin(angle),radius*degreeCos(angle));
		}
	}
	
	public double getAngle(){return angle;}
	public void setAngle(double anAngle)
	{
		angle = anAngle % 360;
		setShip();
	}
	public void moveAngle(double turnAngle)
	{
		if (isActive())
		{
			angle += turnAngle;
			angle %= 360;
			setShip();
		}
	}
	
	public Line2D[] getLines(){return lines;}
	public Bullet[] getBullets(){return bullets;}
	public int getScore(){return score;}
	public void addScore(int i){score+=i;}
	public void decScore(int i){score-=i;}
	public boolean isShielded(){return shielded>=0;}
	public boolean spawning(){return deadTime>=0;}
	public double time_to_spawn(){return spawnTime - deadTime;}
	public boolean isActive(){return !(spawning() || paused()) && inUse();}
	public void setPaused(boolean b){paused = b;}
	public boolean paused(){return paused;}
	public int getLives(){return lives;}
	public boolean isDead(){return lives<0;}
	
	public void pause()
	{
		if (LinDist(path, previous.getLine())>2*radius) return;
		
		paused = !paused;
		
		if (!paused) 
		{
			this.Destroy();
			//deadTime = -1;
		}
		
	}
	
	public String toString()
	{
		return this.name + "("+lives+"): " + this.score;
	}
	
	public void setBase(Point2D p)
	{
		super.base.setLocation(p);
		base.setBase(p);
	}
	
	public void setBase(Base b)
	{
		previous.leave();
		
		base.setBase(b);
		base.setRealBase(super.base, radius);
		
		previous = b;
		
		b.setShip(this);
		b.setName(name);
		
		team = b.getTeam();
	}
	
	public void Thrust(double power)
	{
		if (isActive())
		Force(degreeSin(angle)*(power)/XPilotServer.FPS,
				degreeCos(angle)*(power)/XPilotServer.FPS);
	}
	
	public void Move()
	{
		if(CoolTime>=0)
		{
			CoolTime+=1.0/XPilotServer.FPS;
		}
		if (CoolTime>CoolDown)
		{
			CoolTime=-1;
		}
		
		if (shielded>=0)
		{
			shielded += 1.0/XPilotServer.FPS;
		}
		if (shielded > ShieldTime)
		{
			shielded = -1;
		}
		if (deadTime >= 0 && !isDead())
		{
			deadTime += 1.0/XPilotServer.FPS;
		}
		if (deadTime>spawnTime)
		{
			deadTime = -1;
			shielded = 0;
		}
		
		//previous.setLocation(center);
		super.Move();
	}
	
	//for regular movement
	public void MoveShip(double dX, double dY)
	{
		setLocation(center.getX()+dX, center.getY()-dY);
	}
	
	public void setLocation(double x, double y)
	{
		super.setLocation(x, y);
		setShip();
	}
	
	public void setLocation(Point2D p)
	{
		super.setLocation(p);
		setShip();
	}
	
	public void Fire(double BulletSpeed)
	{
		if (CoolTime<0 && isActive())
		{
			Bullet b = findAvailableBullet();
			if(b != null)
			{
				CoolTime = 0;
				shielded = -1;
				b.Fire(BulletSpeed, Xspeed, Yspeed, angle, points[0]);
			}
		}
	}
	
	private Bullet findAvailableBullet()
	{
		for (int i =0; i<bullets.length;i++)
		{
			if(!bullets[i].isActive())
			{
				return bullets[i];
			}
		}
		return null;
	}
	
	public void Kill()
	{
		this.Destroy();
		lives--;
	}
	
	public void Destroy()
	{
		angle=base.getAngle();
		CoolTime=-1;
		deadTime = 0;
		shielded = 0;
		super.Destroy();
	}
	
	public void resetShip()
	{
		previous.leave();
		this.Destroy();
		score = 0;
		paused = false;
	}
	
	public void resetRound()
	{
		lives = startLives;
		for (Bullet b : bullets) b.Destroy();
		this.Destroy();
	}
	
	public boolean hitsBase(Base b)
	{
		return LinDist(path, b.getLine()) <= radius;
	}
}
