package XPilot;

import java.awt.geom.*;
import java.util.ArrayList;

//what is passed back and forth between Client and Server
public class XPilotRegister implements java.io.Serializable
{
	GameInfo gameInfo;
	
	ArrayList<Ship> Ships;
	ArrayList<Line2D> Lines;
	Rectangle2D Border;
	ArrayList<Ball> Balls;
	//ArrayList<Rectangle2D> Boxes;
	Point2D Focus;
	int self;
	boolean selfkill;
	public int frame_number=0;
	Base[] Bases;
	
	//constructors
	public XPilotRegister()
	{
		Ships = new ArrayList<Ship>();
		Lines = new ArrayList<Line2D>();
		Border = new Rectangle2D.Double();
		Balls = new ArrayList<Ball>();
		Focus = new Point2D.Double();
	}
	

	XPilotRegister(ArrayList<Ship> aShips, ArrayList<Line2D> aLines, Rectangle2D aBorder, ArrayList<Ball> aBalls, int s, boolean b)
	{
		Ships = new ArrayList<Ship>(aShips.size());
		for (int i = 0; i< aShips.size(); i++)
		{
			Ships.add(i, Ship.copy(aShips.get(i)));
		}
		Ships.trimToSize();
		
		Lines = new ArrayList<Line2D>(aLines.size());
		for (int i = 0; i< aLines.size(); i++)
		{
			Lines.add(i, new Line2D.Double());
			Lines.get(i).setLine(aLines.get(i));
		}
		Lines.trimToSize();
		
		if (aBalls!=null)
		{
		Balls = new ArrayList<Ball>(aBalls.size());
		for (int i=0;i<aBalls.size();i++)
		{
			Balls.add(i, new Ball(aBalls.get(i)));
		}
		Balls.trimToSize();
		} else {Balls = null;}
		
		
		Border = new Rectangle2D.Double();
		Border.setFrame(aBorder);
		Focus = new Point2D.Double();
		
		if (s>=0 && s<Ships.size())
		{
		self = s;
		Focus.setLocation(Ships.get(self).getCenter());
		}
		
		selfkill=b;
	}

	

	//creates a copy of another register
	XPilotRegister(XPilotRegister other)
	{
		this(other.Ships, other.Lines, other.Border, other.Balls,other.Bases, other.self, other.selfkill);
	}

	public XPilotRegister(ArrayList<Ship> aShips, ArrayList<Line2D> aLines, Rectangle2D aBorder, ArrayList<Ball> aBalls, Base[] aBases, int s, boolean b)
	{
		this(aShips,aLines,aBorder,aBalls,s,b);
		Bases = aBases;
		
	}
	
	public void setRegisterDirect(ArrayList<Ship> aShips, ArrayList<Line2D> aLines, Rectangle2D aBorder, ArrayList<Ball> aBalls, int s, boolean b)
	{
		Ships = aShips;
		Lines = aLines;
		Border = aBorder;
		Balls = aBalls;
		
		if (s>=0 && s<Ships.size())
		{
		self = s;
		Focus = Ships.get(s).getCenter();
		}
		selfkill=b;
	}
	
	public void setRegisterDirect(ArrayList<Ship> aShips, ArrayList<Line2D> aLines, Rectangle2D aBorder, ArrayList<Ball> aBalls, int s, boolean b, GameInfo info, Base[] Bases)
	{
		this.setRegisterDirect(aShips, aLines, aBorder, aBalls, s, b);
		this.gameInfo=info;
		this.Bases = Bases;
	}
	
	/*
	public void setRegister(ArrayList<Ship> aShips, ArrayList<Line2D> aLines, Rectangle2D aBorder, ArrayList<Ball> aBalls, int s, boolean b)
	{
		if (aShips.size() == Ships.size())
		{
			for (int i = 0; i<aShips.size(); i++)
			{
				Ship ship = aShips.get(i);
				if (ship!=null)
				{
					Ships.get(i).setShip(aShips.get(i));
				}
			}
		}
		else
		{
			Ships.clear();
			Ships.ensureCapacity(aShips.size());
			for (int i = 0; i<aShips.size(); i++)
			{
				if (i<Ships.size())
				{
					Ships.get(i).setShip(aShips.get(i));
				}
				else
				{
					Ships.add(i, new Ship(aShips.get(i)));
				}
				
			}
			Ships.trimToSize();
		}
		
		if (aLines.size() == Lines.size())
		{
			for (int i = 0; i< aLines.size(); i++)
			{
				Lines.get(i).setLine(aLines.get(i));
			}
		}
		else 
		{
			Lines.clear();
			Lines.ensureCapacity(aLines.size());
			for (int i = 0; i<aLines.size(); i++)
			{
				Lines.get(i).setLine(aLines.get(i));
			}
			Lines.trimToSize();
		}
		
		if (aBalls.size()==Balls.size())
		{
			for (int i = 0; i<aBalls.size();i++)
			{
				Balls.get(i).setBall(aBalls.get(i));

			}
		}
		else
		{
			Balls.clear();
			Balls.ensureCapacity(aBalls.size());
			for (int i = 0; i<aBalls.size(); i++)
			{
				Balls.get(i).setBall(aBalls.get(i));
			}
			Balls.trimToSize();
		}

		if (aBoxes.size()==Boxes.size())
		{
		for (int i=0;i<aBoxes.size();i++)
		{
			Boxes.get(i).setFrame(aBoxes.get(i));
		}

		
		Border.setFrame(aBorder);
		
		if (s>=0 && s<Ships.size())
		{
		self = s;
		Focus = Ships.get(s).getCenter();
		}
		selfkill=b;
	}
	*/
	
	//creates a copy of the register
	/*
	public void setRegister(XPilotRegister other, int s)
	{
		setRegister(other.Ships, other.Lines, other.Border, other.Balls, s, other.selfkill);
		frame_number = other.frame_number;
	}
	*/
	
	public void setRegisterDirect(XPilotRegister other, int s)
	{
		setRegisterDirect(other.Ships, other.Lines, other.Border, other.Balls, s, other.selfkill, other.gameInfo, other.Bases);
		frame_number = other.frame_number;
	}
	
	/*
	public void setRegister(XPilotRegister other)
	{
		setRegister(other, other.self);
	}
	*/
	
	public Ship getSelf()
	{
		if (self>=0)
		return Ships.get(self);
		else return null;
	}
}