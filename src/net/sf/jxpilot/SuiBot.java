package net.sf.jxpilot;

import java.awt.geom.*;

import static net.sf.jxpilot.MathFunctions.*;

public class SuiBot implements XPClient
{
	private XPilotRegister register;
	private Ship target;
	private Ship self;
	private Line2D path;
	private double distance = 0;
	private double MapW, MapH;
	private boolean targeting=false;
	
	public SuiBot()
	{
		path = new Line2D.Double();
	}
	
	public SuiBot(XPilotRegister r)
	{
		register = r;
		path = new Line2D.Double();
	}
	
	public void setRegister(XPilotRegister r)
	{
		register = r;
		self = register.Ships.get(register.self);
		MapW = register.Border.getWidth();
		MapH = register.Border.getHeight();
		acquireTarget();
	}
	public XPilotRegister getRegister() {return register;}
	public double turnShip()
	{
		if (target!=null)
		return (90-LinAngle(path))-self.getAngle();
		else return 0;
	}
	public boolean Thrusts(){return target!=null;}
	public boolean Fires(){return target!=null;}
	//public Point2D getCenter(){return center;}
	public boolean Attaches(){return false;}
	public boolean Detaches(){return false;}
	public String getName(){return "SUIBOT";}
	public boolean switchTeams(){return false;}
	public boolean isRobot(){return true;}
	public boolean takeBase(){return false;}
	public boolean pauses(){return false;}
	
	public void close()
	{
		
	}
	
	
	private void acquireTarget()
	{
		target = null;
		
			distance =Double.MAX_VALUE;
			for (Ship s : register.Ships)
			{
				if (s.team!=self.team && !s.isShielded() && !s.spawning() && s.inUse())
				{
					WrapLineClose(path, self.getCenter(), s.getCenter(), MapW, MapH);
					
					if (distance>LinLength(path))
					{
						boolean valid = true;
						for (Line2D l : register.Lines)
						{
							if (path.intersectsLine(l))
							{
								valid=false;
							}
							wrapLine(path, MapW, MapH);
							if (path.intersectsLine(l))
							{
								valid=false;
							}
						}
						if (valid)
						{
							target=s;
							targeting = true;
						}
					}
				}
			}
		
	
		if (target!=null)
		{
			WrapLineClose(path, self.getCenter(), target.getCenter(), MapW, MapH);
		}
	}
	
}
