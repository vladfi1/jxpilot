package XPilot;

import java.awt.geom.*;
import javax.swing.Timer;
import java.awt.event.*;
import java.util.ArrayList;
import static XPilot.MathFunctions.*;

/*
abstract class XPServer
{
	//interaction with client
	public void giveRegister(XPClient client, XPilotRegister aRegister)
		{client.setRegister(aRegister);}
}
*/

interface XPServer
{
	public void giveRegister(XPClient client, XPilotRegister aRegister);
	public double MapWidth();
	public double MapHeight();
	public final static int FPS=20;
}

public class XPilotServer implements ActionListener, XPServer
{
	//variables and constants
	
	public static final int LOCAL = 0;
	public static final int REMOTE = 1;
	public static final int ROBOT = 2;
	private ServerInterface SI;
	
	private int MAPNUMBER =0;
	
	private int NUMLINES;
	private int NUMSHIPS = 1;
	private int MAX_CLIENTS;
	//private final int MAX_TEAM = 1;
	private final int NUM_TEAMS = 2;
	private int NUMREGISTERS = NUMSHIPS;
	private int NUMCLIENTS = NUMREGISTERS;
	private int NUMBALLS;
	
	private final double SHIP_RADIUS = 10;
	private final double BULLET_RADIUS=2;
	private final double SHIP_MASS = 1;
	private final double POWER = 700;
	private final int SHOTLIMIT = 20;
	private final double BULLETSPEED=300;
	public static final double BULLETLIFE=5;
	public static final double COOLDOWN = .1;
	public final double SHIELD_TIME = 2.5;
	private final double BALL_RADIUS = 7;
	private final double BALL_MASS = 2.5;
	private final double CONNECTOR_LENGTH = 95;
	private final double TENSION = 20000;
	private final double SPAWN_TIME = 2;
	
	private double SHIPFRICTION = 60;
	private double BALLFRICTION = 100;
	
	public static final boolean SELFKILL=true;
	public boolean teamplay;
	public static boolean breakable=false;
	
	//constants
	private static final int BALL_CASH = 30;
	private static final int BALL_REPLACE = 5;
	private final int SHIP_CRASH = 3;
	private final int SELF_KILL = 5;
	private final int KILL = 10;
	public final int LIVES = 3;
	
	//objects in virtual world
	private XPilotMap Map;
	private ArrayList<Ship> Ships;
	private Ship[][] Teams;
	private WallHit HIT;
	private ArrayList<XPClient> Clients;
	private ArrayList<Line2D> Lines;
	private Base[] Bases;
	
	//the images are like this:
	/*
	 * 0 1 2
	 * 3 4 5
	 * 6 7 8
	 */
	
	private Line2D[][] WallImages;
	
	private Rectangle2D Border;
	private ArrayList<Ball> Balls;
	private ArrayList<XPilotRegister> Registers;
	private XPilotRegister Register;
	private String gameInfo="";
	private GameInfo info;
	
	//private ClientHandler.ClientInterface c;
	private ClientHandler Handler;
	
	public void giveRegister(XPClient client, XPilotRegister aRegister)
	{client.setRegister(aRegister);}
	
	public double MapWidth(){return Map.getBorder().getWidth();}
	public double MapHeight(){return Map.getBorder().getHeight();}
	
	public XPilotRegister getRegister(int i)
	{
		if (i>=0 && i<Registers.size())
		{
			return Registers.get(i);
		}
		
		return null;
	}
	
	public XPClient getClient(int i)
	{
		if (i>=0 && i<Clients.size())
		{
			return Clients.get(i);
		}
		
		return null;
	}
	
	//initializes server
	XPilotServer()
	{	
		
		setMap(MAPNUMBER);
		Border = Map.getBorder();
		setLines();
		setShips();
		setBalls();
		setBases();
		HIT = new WallHit();
		
		setRegisters();
		setClients();
		
		Handler = new ClientHandler(this);
		//c = new ClientHandler().new ClientInterface(Registers.get(0));
		
		info = new GameInfo(Teams);
		
		Thread t = new Thread(Handler);
		t.start();
		
		SI = new ServerInterface(this);
		Thread t2 = new Thread(SI);
		t2.start();
		
	}

	//helper methods for constructor
	private void setMap(int i)
	{
		Map = XPilotMap.getMap(i);
		teamplay=Map.teams();
		MAX_CLIENTS = Map.NumBases();
	}
	
	private void setBases()
	{
		Bases = Map.getBases();
		Base.setRadius(SHIP_RADIUS);
		
		for (Base b : Bases)
		{
			b.setLine();
		}
		
	}
	
	private void setLines()
	{
		//sets lines
		NUMLINES = Map.NumWalls();
		Lines = new ArrayList<Line2D>(NUMLINES);
		
		for(int i = 0; i<NUMLINES;i++)
		{
			Lines.add(i, Map.getWall(i));
		}
		
		Lines.trimToSize();
		
		WallImages = new Line2D[9][Lines.size()];
		for (int i =0;i<9;i++)
		{
			for (int j=0;j<WallImages[i].length;j++)
			{
				WallImages[i][j]=new Line2D.Double();
			}
		}
		setImages();
	}
	
	private void setImages()
	{
		double dX, dY;
		double MapW = Map.getBorder().getWidth();
		double MapH = Map.getBorder().getHeight();
		
		for (int i=0;i<9;i++)
		{
			dX = ImageX(i)*MapW;
			dY = ImageY(i)*MapH;
			for (int j=0;j<WallImages[i].length;j++)
			{
				WallImages[i][j].setLine(Lines.get(j));
				translateLine(WallImages[i][j], dX, -dY);
			}
		}
	}
	
	private int ImageX(int i)
	{
		return (i%3-1);
	}
	
	private int ImageY(int i)
	{
		return ((int)(i/3)-1);
	}
	
	private void setBalls()
	{
		NUMBALLS = Map.NumBalls();
		Balls = new ArrayList<Ball>(NUMBALLS);
		for(int i=0; i<NUMBALLS;i++)
		{
			Balls.add(i, new Ball(BALL_RADIUS, CONNECTOR_LENGTH, Map.getBall(i),BALL_MASS, TENSION));
		}
		
		Balls.trimToSize();
		
		Ball.setHeight(MapHeight());
		Ball.setWidth(MapWidth());
		Ball.setFPS(FPS);
		
		for (int i=0;i<Map.getBallTeams().length;i++)
		{
			Balls.get(i).setTeam(Map.getBallTeams()[i]);
		}
		
	}
	
	private Ship defaultShip()
	{
		return new Ship(SHIP_RADIUS, 0, SHIP_MASS, SHOTLIMIT, BULLET_RADIUS, COOLDOWN, SHIELD_TIME, SPAWN_TIME);
	}
	
	private void setShips()
	{
		
		//sets ships
		Ships = new ArrayList<Ship>(MAX_CLIENTS);
		
		for (int i =0;i<MAX_CLIENTS;i++)
		{
			Ships.add(i, defaultShip());
		}
		
		Ships.trimToSize();

		Ship.setHeight(MapHeight());
		Ship.setWidth(MapWidth());
		Ship.setFPS(FPS);
		Ship.setStartLives(LIVES);
		Bullet.setHeight(MapHeight());
		Bullet.setWidth(MapWidth());
		Bullet.setFPS(FPS);
		
		Teams = new Ship[NUM_TEAMS][];
		
		for (int t = 0;t<Teams.length;t++)
		{
			Teams[t] = new Ship[Map.numBases(t)];
		}

		setTeams();
	}
	
	private void setTeams()
	{
		EraseTeams();
		for (int i = 0; i<Ships.size();i++)
		{
			Ship s = Ships.get(i);
			
			if (s.inUse())
			{
				Teams[s.getTeam()-1][nextTeam(s.getTeam())] = s;
			}
		}
	}
	
	private int nextTeam(int team)
	{
		Ship s;
		for (int i = 0; i<Teams[team-1].length;i++)
		{
			s = Teams[team-1][i];
			
			if (s==null || !s.inUse())
			{
				return i;
			}
		}
		
		return -1;
	}
	
	private void EraseTeams()
	{
		for (int t = 0; t< Teams.length;t++)
		{
			for (int i = 0; i<Teams[t].length; i++)
			{
				Teams[t][i]=null;
			}
		}
	}
	
	private XPilotRegister defaultRegister()
	{
		return new XPilotRegister(Register);
	}
	
	private void setRegisters()
	{
		Register = new XPilotRegister(Ships, Lines, Border, Balls, Bases , -1, false);
		Registers = new ArrayList<XPilotRegister>(MAX_CLIENTS);
		for (int i =0; i<MAX_CLIENTS; i++)
		{Registers.add(i, defaultRegister());}
		Registers.trimToSize();
	}
	
	private void setClients()
	{
		//sets clients
		Clients = new ArrayList<XPClient>(MAX_CLIENTS);
		//local client
		//Clients.add(0, new XPilotClient(Registers.get(0)));
		for (int i =0; i< MAX_CLIENTS; i++)
		{
			Clients.add(i, null);
		}
		
		
		/*
		for (int i = 1; i<NUMCLIENTS; i++)
		{Clients.add(i, new SuiBot(Registers.get(i)));}
		Clients.trimToSize();
		*/
		
		/*
		for (int i = 0; i<NUMCLIENTS; i++)
		{
			Ships.get(i).name=Clients.get(i).getName();
		}
		*/
		
		setArrays();
		
	}
	
	private void setArrays()
	{
		NUMCLIENTS = 0;
		for (XPClient c : Clients)
		{
			if (c!= null)
			{
				NUMCLIENTS++;
			}
		}
		
		NUMSHIPS = 0;
		for (Ship s : Ships)
		{
			if (s.inUse())
			{
				NUMSHIPS++;
			}
		}
		
		NUMREGISTERS = 0;
		for (XPilotRegister r : Registers)
		{
			if (r!=null)
			{
				NUMREGISTERS++;
			}
		}
	}
	
	public int addClient(int type)
	{
		int next = nextClient();
		
		if (next>=0)
		{
			//Registers.set(next, defaultRegister());
			
			Ship s = Ships.get(next);
			Base b;
			
			
			if (numPlayers(1)>numPlayers(2))
			{
				b = this.getNextBase(2);
				if (b==null)
				{
					b = this.getNextBase(1);
				}
			}
			else
			{
				b = this.getNextBase(1);
				if (b==null)
				{
					b = this.getNextBase(2);
				}
			}
			
			s.resetShip();
			
			s.setBase(b);
			
			int t =b.getTeam();
			
			s.setTeam(t);
			
			boolean reset = false;
			if (numActivePlayers(t)==0)
			{
				reset = true;
			}
			
			s.setUse(true);
			
			s.Destroy();
			
			Registers.get(next).setRegisterDirect(Register, next);

			if (type == this.LOCAL)
				 Clients.set(next, new XPilotClient(Registers.get(next), this));
			else if(type == this.REMOTE)
				Clients.set(next, new ClientInput());
			else if (type == this.ROBOT)
				Clients.set(next, new SuiBot(Registers.get(next)));
				
			setArrays();
			setTeams();
			
			if (reset) this.resetRound();
			
		}	
		
		return next;
	}
	
	private int numPlayers()
	{
		int x=0;
		for (int i=0;i<Ships.size();i++)
		{
			if (Ships.get(i).inUse())
			{
				x++;
			}
		}
		return x;
	}
	
	private int nextClient()
	{
		for (int i = 0; i< Ships.size();i++)
		{
			if (!Ships.get(i).inUse())
			{
				return i;
			}
		}
		return -1;
	}
	
	public void removeClient(int i)
	{
		Ships.get(i).setUse(false);
		Ships.get(i).resetShip();
		
		setArrays();
		setTeams();
	}
	
	public void removeRobots()
	{
		for (int i = 0; i<Clients.size();i++)
		{
			if (Clients.get(i).isRobot())
			{
				removeClient(i);
			}
		}
	}
	
	public void removeNextRobot()
	{
		for (int i = 0; i<Clients.size();i++)
		{
			if (Clients.get(i).isRobot())
			{
				removeClient(i);
				break;
			}
		}
	}
	
	private int numPlayers(int team)
	{
		team--;
		if (team>=0 && team < Teams.length)
		{
			return numPlayers(Teams[team]);
		}
		else
		{
			System.out.println("Invalid team");
			
			return -1;
		}
	}
	
	private int numActivePlayers(int team)
	{
		team--;
		if (team>=0 && team < Teams.length)
		{
			return numActivePlayers(Teams[team]);
		}
		else
		{
			System.out.println("Invalid team");
			
			return -1;
		}
	}
	
	private int numPlayers(Ship[] team)
	{
		int x = 0;
		for (Ship s : team)
		{
			if (s!=null && s.inUse())
			{
				x++;
			}
		}
		return x;
	}
	
	private int numActivePlayers(Ship[] team)
	{
		int x = 0;
		for (Ship s : team)
		{
			if (s!=null && s.inUse() && !s.paused())
			{
				x++;
			}
		}
		return x;
	}
	
	private Base getNextBase(int team)
	{
		for (Base b: Bases)
		{
			if (b.getTeam()==team && !b.isTaken())
			{
				return b;
			}
		}
		return null;
	}
	
	private void switchTeams(Ship s)
	{
		int newTeam = 3 - s.getTeam();
		Base b = getNextBase(newTeam);
		
		if (b!=null)
		{
			s.setBase(b);
			this.setTeams();
		}
	}
	
	private void pauseShip(Ship s)
	{
		s.pause();
		//System.out.println("pausing");
	}
	
	private boolean roundOver()
	{
		for (Ship[] team : Teams)
		{
			if (numPlayers(team)==0) continue;
			
			boolean dead = true;
			
			for (Ship s : team)
			{
				if (s!=null)
				dead = dead && s.isDead();
			}
			
			if (dead) return true;
		}
		return false;
	}
	
	private void resetRound()
	{
		for (Ball b : Balls) b.Destroy();
		for (Ship s : Ships) s.resetRound();
	}
	
	//starts game loop
	public static void main(String[] args)
	{
		ActionListener l = new XPilotServer();
		Timer t = new Timer((int)1000/FPS, l);
		t.start();
	}
	
	static int time=0;
	static long time2;
	//game loop
	public void actionPerformed(ActionEvent e)
	{

		//System.out.println(System.currentTimeMillis()-time2);
		//time2=System.currentTimeMillis();
		time++;
		if (time == FPS)
		{
			//System.out.println(System.currentTimeMillis());
			time =0;
		}
		
		//all client input
		for (int i=0; i<Clients.size(); i++)
		{
			if (Clients.get(i)!=null)
			{
				
				Ships.get(i).moveAngle(Clients.get(i).turnShip());
				
				if(Clients.get(i).Fires())
					{Ships.get(i).Fire(BULLETSPEED);}
				
				if (Clients.get(i).Thrusts())
					{Ships.get(i).Thrust(POWER);}
								
				Ships.get(i).name = Clients.get(i).getName();
			}
			
		}
		
		//ball-ship interaction
		for (int i=0;i<Clients.size();i++)
		{
			XPClient c = Clients.get(i);
			if (c != null && !Ships.get(i).spawning())
			{
			if (c.Attaches())
			{
				//System.out.println(i+"is attaching");
				for (Ball b : Balls)
				{
					b.Connect(Ships.get(i));
				}
			}
			else {
				for (Ball b :Balls)
				{
					if (b.isAttachedTo(Ships.get(i)) && b.isConnecting())
					{
						b.disConnect();
					}
				}
			}
			
			if (c.Detaches())
			{
				for (Ball b : Balls)
				{
					if (b.isAttachedTo(Ships.get(i)))
					{
						b.disConnect();
					}
				}
			}
			
			if (c.switchTeams())
			{
				this.switchTeams(Ships.get(i));
			}
			
			if (c.takeBase())
			{
				for (Base b : Bases)
				{
					Ship s = Ships.get(i);
					if (!b.isTaken() && b.getTeam() == s.getTeam() && s.hitsBase(b))
					{
						s.setBase(b);
					}
				}
			}
			
			if (c.pauses())
			{
				pauseShip(Ships.get(i));
			}
			
			}
		}
		
		for (Ball b : Balls){b.ConnectorForce();}
		
		//moves entities
		moveBullets();
		moveShips();
		moveBalls();
		
		BulletCollisions();
		ShipCollisions();
		BallCollisions();
		
		//sets entities after bounces
		for (Ship s : Ships)
		{
			if (s!=null)
			{
			s.setShip();
			for (Bullet b : s.getBullets())
			{
				b.setBullet();
			}
			}
		}
		
		for (Ball b : Balls)
		{
			b.setBall();
		}
		
		setInfo();
		
		//sets registers for clients
		Register.setRegisterDirect(Ships, Lines, Border, Balls, -1,SELFKILL, info, Bases);
		Register.frame_number++;
		

		
		//System.out.println(Register.Bases == null);
		
		for (int i =0; i<Registers.size(); i++)
		{
			XPilotRegister r = Registers.get(i);
			if (r!=null)
			synchronized (r)
			{
				r.setRegisterDirect(Register, i);
				//System.out.println(r.Bases==null);
			}
		}
		
		//gives registers to clients
		for (int i = 0; i<NUMCLIENTS; i++)
		{giveRegister(Clients.get(i), Registers.get(i));}
		
		//c.sendRegister();
		
		Handler.send();
		
		x++;
		if (x%20==0)
		{
			//System.out.println("Sent register " + Register.frame_number + " at: " + (System.currentTimeMillis()-t));	
		}
		
		if (roundOver()) resetRound();
	}
	
	int x;
	long t= System.currentTimeMillis();;
	
	private void setInfo()
	{
		gameInfo="";
		for (int t=0;t<Teams.length;t++)
		{
			gameInfo += "Team "+(t+1)+"\n";
			for (Ship s: Teams[t])
			{
				if (s!=null)
				{
					gameInfo += s + "\n";
				}
			}
		}
		
		info.setInfo(Teams);
		
	}
	
	private void moveShips()
	{
		for (Ship s : Ships)
		{
			if (s.isActive() || s.spawning())
				s.Move();
		}
	}
	
	private void moveBullets()
	{
		for (Ship s : Ships)
		{
			if (s.inUse())
			{
				for (Bullet b : s.getBullets())
				{
					if (b.isActive())
					{
						b.Move();
				//double x = b.getCenter().getX();
				//double y = b.getCenter().getY();
				
				//b.setLocation(trueMod(x, Border.getWidth()), trueMod(y, Border.getHeight()));
					}
				}
			}
		}
	}
	
	private void moveBalls()
	{
		for (Ball b: Balls)
		{
			b.Move();
			//double x = b.getCenter().getX();
			//double y = b.getCenter().getY();
			
			//b.setLocation(trueMod(x, Border.getWidth()), trueMod(y, Border.getHeight()));
		}
	}
	
	//quick function to test wall hits if no other info needed
	private boolean hitsWalls(BaseEntity b)
	{
		
		for(Line2D[] l : WallImages)
		{
			for(Line2D l2 : l)
			{
				if(b.WallCollision(l2).hit_type()!=WallHit.NO_HIT)
				{
					return true;
				}
			}
		}
		
		return false;
		
	}
	
	private void BulletCollisions()
	{
		boolean cont;
		
		for (Ship s : Ships)
		{
			if (s.inUse())
			{
			for (Bullet b : s.getBullets())
			{
				if (b.isActive())
				{
					cont =false;
					
						/*
						if (LinDist(b.getPath(), l) < b.getRadius())
						{
							b.Destroy();
							cont =true;
						}
						*/
						
					if (hitsWalls(b))
						{
							b.Destroy();
							continue;
						}
					
					for (Ball ball : Balls)
					{
						if (ball.BoxCollision(b))
						{
							b.Destroy();
							continue;
						}
					}
					
					if (SELFKILL)
						{
							for (Ship s2 : Ships)
							{
								if (s2.isActive())
								if (!(b.FirstShot() && (s==s2)) && !((s!=s2) && (s.team==s2.team)))
								{
									if (b.Collision(s2))
									{	
										if (!(s2.isShielded()))
										{
											Kill(s2, s);
											
										}
										b.Destroy();
									}
								}
							}
					}else{
						for (Ship s2 : Ships)
						{
							if(s2.isActive())
							if (s2.team!=s.team)
							{
								if (b.Collision(s2))
								{
									if (!(s2.isShielded()))
									{
										Kill(s2, s);
									}
									b.Destroy();
								}
							}
						}
					}
				}
			}
		}
		}
	}
	
	private void Kill(Ship s, Ship killer)
	{
		
		if (s==killer)
		{
			s.decScore(SELF_KILL);
		}
		else
		{
			s.decScore(KILL);
			killer.addScore(KILL);
		}

		Kill(s);
	}
	
	private void Kill(Ship s)
	{
		for (Ball b : Balls)
		{
			if (b.isAttachedTo(s))
			{
				b.disConnect();
			}
		}
		s.Kill();
	}
	
	private void ShipCollisions()
	{
		for (Ship s : Ships)
		{	
			if (s.isActive())
			{
			boolean cont = false;
			for (Ship s2 : Ships)
			{
				if (s2.isActive())
				{
				if (s2.getTeam() != s.getTeam())
				{
					if (s2.Collision(s))
					{
						if(!s.isShielded())
						{
							s.decScore(SHIP_CRASH);
							Kill(s);
						}
						
						if(!s2.isShielded())
						{
							s2.decScore(SHIP_CRASH);
							Kill(s2);
						}
						
						cont = true;
						break;
					}
				}
				}
			}
			if (cont) continue;
			
			for (Ball b : Balls)
			{
				
				if(b.BoxCollision(s))
				{
					s.decScore(SHIP_CRASH);
					Kill(s);
					cont = true;
					break;
				}
				
				Ship a = b.attached();
				if (a==null)
				{
					if (s.Collision(b))
					{
						b.Destroy();
					}
				}
				else if (a.getTeam()!= s.getTeam())
				{
					if (s.Collision(b))
					{
						b.Destroy();
					}
				}
			}
			if(cont) continue;
			
			BounceCollisions(s, -1, SHIPFRICTION);
		}
		}
	}
	
	//rechecks collisions after each bounce
	private void BounceCollisions(BaseEntity b, int LastHit, double friction)
	{
		int LINE;
		double distance;
		
		do
		{
		LINE=-1;
		distance = Double.MAX_VALUE;
		
		
		//first checks all walls
		for (int image=0;image<WallImages.length;image++)
		{
			for (int line=0;line<WallImages[image].length ; line++)
			{
				int line_int = LineToInt(image, line);
				
				//makes sure that it doesn't bounce the same wall twice
				if(line_int != LastHit)
				{
					Line2D l = WallImages[image][line];
				
					WallHit w = b.WallCollision(l);
					
					int x = w.hit_type();
				
					if (x != WallHit.NO_HIT)
					{
						//only uses a wall if it is closer than the previous wall,
						//this has the effect that the closest wall will be found in the end
						if (w.distance<distance)
						{
							//if so, records which wall was hit and relevant info about the hit
							LINE = line_int;
							distance = w.distance;
							HIT.setHit(w); //stores the wall hit for further use... WallHit w is lost later (has scope inside the loop)
						}
					}
				}
			}
		}
		
		//if any wall was recorded as a hit, bounces of of it
		if (LINE>=0)
		{
			b.Bounce(HIT, friction);
			//rechecks collisions
			
			LastHit = LINE;
			
			//BounceCollisions(b, LINE, friction);
		}
		
		} while (LINE>=0);
	}
	
	private int imageFromLine(int line)
	{
		return line/WallImages.length;
	}
	private int lineFromLine(int line)
	{
		return line - NUMLINES*imageFromLine(line);
	}
	private int LineToInt(int image,int line)
	{
		return image*NUMLINES+line;
	}
	
	private void BallCollisions()
	{
		for (Ball b : Balls)
		{
			if (b.thrown() != null)
			{
			//b.setHitPath();
			
			for (Ball b2 : Balls)
			{
				if (b.thrown().getTeam()==b2.getTeam())
				{
					if (b2.BoxCollision(b))
						{
							if (b2==b)
							{
								b.thrown().addScore(BALL_REPLACE);
							}
							else
							{
								//b.thrown().addScore(30);
								
								if (numPlayers(b.getTeam())>0)
								for (Ship s: Teams[b.thrown().getTeam()-1])
								{
									if (s!=null && s.inUse())
									{
										s.addScore(BALL_CASH);
									}
								}
								
								for (Ship s: Teams[b.getTeam()-1])
								{
									if (s!=null && s.inUse())
									{
										s.decScore(BALL_CASH);
									}
								}
								
								this.resetRound();
							}
							b.Destroy();
							
							break;
						}
				}
			}
			
			}
				BounceCollisions(b, -1, BALLFRICTION);
			}
	}
}