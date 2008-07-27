package XPilot;

import java.awt.geom.*;

public class XPilotMap
{
	private Line2D[] walls;
	private Rectangle2D border;
	private final static int MAPNUMBER=1;
	private Base[] bases;
	private Point2D[] balls;
	private int[] ballTeams;
	private boolean teamPlay;
	
	XPilotMap(Rectangle2D rect, Line2D[] lines, Base[] bases, Point2D[] b, boolean teams, int[] ballTeams)
	{
		border = new Rectangle2D.Double(0,0,0,0);
		border.setFrame(rect);
		
		walls = lines;
		
		/*
		for (int i= 0; i<walls.length; i++)
		{
			walls[i] = new Line2D.Double(0,0,0,0);
			walls[i].setLine(lines[i]);
		}
		*/
		
		this.bases=bases;
		balls =b;
		
		teamPlay=teams;
		this.ballTeams=ballTeams;
		
	}
	
	private static XPilotMap[] Maps = new XPilotMap[MAPNUMBER];
	
	static
	{
		Maps[0] = new XPilotMap( 
				//border
				new Rectangle2D.Double(0,0,2000,1500),
				//walls
				new Line2D[] {
					new Line2D.Double(100,0,100,100),
					new Line2D.Double(100,100,200,200),
					new Line2D.Double(200,200,500,200),
					new Line2D.Double(500, 200, 900, 600),
					new Line2D.Double(200,150,500, 200)},
				//bases
				new Base[] {
					new Base(new Point2D.Double(250,200), 180, 1),
					new Base(new Point2D.Double(100,200), 0, 2),
					new Base(new Point2D.Double(400,500), 45, 2)},
				//balls
				new Point2D[] {
					new Point2D.Double(250, 50),
					new Point2D.Double(500,500)
				/*, new Point2D.Double(300,300)*/},
				//team play
				true,
				//ball teams
				new int[] {1,2}
		);
	}
	
	public static XPilotMap getMap(int i)
	{
		if (i>= 0 && i<Maps.length)
		{
			return Maps[i];
		}
		else return null;
	}
	
	public Line2D getWall(int i)
	{
		if (i>=0 && i<walls.length)
		{return walls[i];}
		else return null;
	}
	
	public Rectangle2D getBorder()
	{return border;}
	
	public int NumWalls()
	{
		return walls.length;
	}
	
	public Base getBase(int i)
	{
		return bases[i];
	}
	
	public Base getNextBase(int team)
	{
		for (Base b: bases)
		{
			if (b.getTeam()==team && !b.isTaken())
			{
				return b;
			}
		}
		return null;
	}
	
	public int NumBases()
	{
		return bases.length;
	}
	
	public Point2D getBall(int i)
	{
		return balls[i];
	}
	
	public int NumBalls()
	{
		if (balls!=null)
			return balls.length;
		else
			return 0;
	}
	
	public int numBases(int team)
	{
		int t=0;
		for (int i = 0;i<bases.length;i++)
		{
			if (bases[i].getTeam()-1==team)
			{
				t++;
			}
		}
		
		return t;
	}
	
	public int[] getBallTeams(){return ballTeams;}
	public boolean teams(){return teamPlay;}
	public Base[] getBases(){return bases;}
}