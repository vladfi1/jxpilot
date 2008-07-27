package jxpilot;

import java.awt.geom.*;


//used to describe wall hits
class WallHit
{
	public static final int NO_HIT=0;
	public static final int WALL1_HIT=1;
	public static final int WALL2_HIT=2;
	public static final int POINT1_HIT=3;
	public static final int POINT2_HIT=4;
	public static final int POINT_HIT=5;
	
	int hit_type;
	double distance;
	Line2D wall;
	Point2D hit;
	
	public WallHit()
	{
		wall = new Line2D.Double();
		hit = new Point2D.Double();
	}
	
	public void setHit(int hit_type, double distance, Line2D wall, Point2D hit)
	{
		if (hit_type>=0 && hit_type<=5 && distance >= 0 && wall!= null && hit!=null)
		{
			this.hit_type=hit_type;
			this.distance=distance;
			this.wall.setLine(wall);
			this.hit.setLocation(hit);
		}
		else
		{
			this.hit_type = NO_HIT;
			this.distance = 0;
		}
	}
	public void setHit(WallHit other)
	{
		setHit(other.hit_type, other.distance, other.wall,other.hit);
	}
	
	public int hit_type()
	{
		return hit_type;
	}
	
}
