package net.sf.jxpilot;

import static net.sf.jxpilot.MathFunctions.*;

import java.awt.geom.*;

class WallHolder
{
	private Line2D wall;
	Line2D wall1, wall2;
	Point2D hit1, hit2, Phit1, Phit2;
	
	WallHolder()
	{
		wall = new Line2D.Double();
		wall1 = new Line2D.Double();
		wall2 = new Line2D.Double();
		hit1 = new Point2D.Double();
		hit2 = new Point2D.Double();
		Phit1 = new Point2D.Double();
		Phit2 = new Point2D.Double();
	}
	
	public void setWall(Line2D wall, double radius)
	{
		setWall(wall);
		setWalls(radius);
	}
	
	public void setWall(Line2D wall)
	{
		this.wall.setLine(wall);
	}
	
	public void setWalls(double radius)
	{
		double dX = radius*(wall.getY2()-wall.getY1())/LinLength(wall);
		double dY = radius*(wall.getX2()-wall.getX1())/LinLength(wall);
		
		wall1.setLine(wall);
		translateLine(wall1, dX, dY);
		wall2.setLine(wall);
		translateLine(wall2,-dX,-dY);
	}
	
	//assumes that walls have already been set
	public WallHit pathHit(WallHit w, Line2D path, double radius)
	{

		double distance=-1, d1, d2, dp1, dp2;
		int type=WallHit.NO_HIT;
		Point2D start = path.getP1();
		
		
		Point2D p1 = intersection(path, wall1);

		if (p1!=null)
		{
			hit1.setLocation(p1);
			d1= start.distance(hit1);
		}
		
		else {d1=Double.MAX_VALUE;}
		
		Point2D p2 = intersection(path, wall2);
		if (p2!=null)
		{
			hit2.setLocation(p2);
			d2= start.distance(hit2);
		}
		else {d2=Double.MAX_VALUE;}
		

		Point2D Hit1 = ClosestLineCircleIntersection(path,wall.getP1(),radius);
		if (Hit1 != null)
		{
			Phit1.setLocation(Hit1);
			dp1 = start.distance(hit1);
		}
		else
		{
			dp1 = Double.MAX_VALUE;
		}
		
		Point2D Hit2 = ClosestLineCircleIntersection(path,wall.getP2(),radius);
		if (Hit2 != null)
		{
			Phit2.setLocation(Hit2);
			dp2 = start.distance(hit2);
		}
		else
		{
			dp2 = Double.MAX_VALUE;
		}
		
		if (p1==null && p2 ==null && Hit1 == null && Hit2 == null)
		{
			w.setHit(type, distance, null, null);
			return w;
		}
		else
		{

			Point2D hit=null;
			Line2D l=null;

			type = WallHit.WALL1_HIT;
			distance = d1;
			
			if (d2<distance)
			{
				type = WallHit.WALL2_HIT;
				distance = d2;
			}
			if (dp1<distance)
			{
				type = WallHit.POINT1_HIT;
				distance = dp1;
			}
			if (dp2<distance)
			{
				type = WallHit.POINT2_HIT;
				distance = dp2;
			}
			
			switch(type)
			{
			case WallHit.WALL1_HIT:
				distance = d1;
				hit = hit1;
				l = wall1;
				break;
			case WallHit.WALL2_HIT:
				distance = d2;
				hit = hit2;
				l = wall2;
				break;	
			case WallHit.POINT1_HIT:
				hit = Phit1;
				distance = dp1;
				l=w.wall;
				MathFunctions.perpendicular(l, hit, wall.getP1());
				break;
			case WallHit.POINT2_HIT:
				hit = Phit2;
				distance = dp2;
				l=w.wall;
				MathFunctions.perpendicular(l, hit, wall.getP2());
				//System.out.println("Point hit");
				break;
			}	
				
			w.setHit(type, distance, l, hit);
			return w;
			
		}
	}
}