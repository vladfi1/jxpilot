package XPilot;

import java.awt.geom.*;

public class MathFunctions
{
	//temporary line/points
	private static Point2D p1, p2;
	private static Line2D l1;
	private static Equation e1, e2;
	
	static
	{
		p1 = new Point2D.Double();
		p2 = new Point2D.Double();
		l1 = new Line2D.Double();
		e1 = new Equation();
		e2 = new Equation();
	}
	
	//useful math functions
	public static double degreeSin(double angle){return Math.sin(Math.toRadians(angle));}
	public static double degreeCos(double angle){return Math.cos(Math.toRadians(angle));}
	
	public static double trueMod(double x, double m)
	{
		return ((x%m)+m)%m;
	}
	
	//translate functions take into account inverted y-axis
	public static Point2D translatePoint(Point2D p, double dX, double dY)
	{
		p.setLocation(p.getX()+dX, p.getY()-dY);
		return p;
	}
	
	//uses clock angle
	public static Point2D translatePointAngle(Point2D p, double d, double angle)
	{
		double dX = d*degreeSin(angle);
		double dY = d*degreeCos(angle);
		
		translatePoint(p, dX, dY);
		
		return p;
	}
	
	public static Line2D translateLine(Line2D l, double dX, double dY)
	{
		l.setLine(translatePoint(l.getP1(), dX, dY), translatePoint(l.getP2(),dX,dY));
		return l;
	}
	//these do not invert y-axis
	public static Point2D TrueTranslate(Point2D p, double dX, double dY)
	{
		return translatePoint(p, dX, -dY);
	}
	public static Line2D TrueTranslate(Line2D l, double dX, double dY)
	{
		return translateLine(l, dX, -dY);
	}
	public static double LinLength(Line2D l)
	{
		return (l.getP1().distance(l.getP2()));
	}
	public static double LinAngle(Line2D l)
	{
		double x1=l.getX1();
		double y1=-l.getY1();
		double x2=l.getX2();
		double y2=-l.getY2();
		
		if (x1==x2)
		{
			if(y1<y2)
			{
				return 90.0;
			}
			else return 270.0;
		}
		else
		{
			if (x1<x2)
			{
				return Math.toDegrees(Math.atan((y2-y1)/(x2-x1)));
			}
			else
			{
				return 180+Math.toDegrees(Math.atan((y2-y1)/(x2-x1)));
			}
		}
	}
	
	public static boolean isPoint(Line2D l)
	{
		return LinLength(l)< .05;
	}
	
	public static double LinDist(Line2D l1, Line2D l2)
	{
		if (!isPoint(l1) && !isPoint(l2) && l1.intersectsLine(l2)) {return 0.0;}
		else 
			return Math.min(Math.min(l1.ptSegDist(l2.getP1()), l1.ptSegDist(l2.getP2())),
					Math.min(l2.ptSegDist(l1.getP1()), l2.ptSegDist(l1.getP2())));
	}
	
	public static void wrapLine(Line2D l, double width, double height)
	{
		double DX = l.getX2()-trueMod(l.getX2(), width);
		double DY = -(l.getY2()-trueMod(l.getY2(), height));
		
		translateLine(l,-DX,-DY);
	}
	
	//gets shortest line from P1 to P2 in a wrapping world
	public static void WrapLineClose(Line2D l, Point2D P1, Point2D P2, double W, double H)
	{
		double dX = (P1.getX()-P2.getX());
		double dW = 0;
		
		if (dX>W/2) {dW=W;}
		else if (dX<-W/2) {dW=-W;}
		
		double dY = (P1.getY()-P2.getY());
		double dH = 0;
		
		if (dY>H/2) {dH=H;}
		else if (dY<-H/2) {dH=-H;}
		
		l.setLine(P1.getX(), P1.getY(), P2.getX()+dW, P2.getY()+dH);
	}
	
	public static void WrapLineClose(Line2D l, double W, double H)
	{
		WrapLineClose(l, l.getP1(), l.getP2(), W, H);
	}
	
	//reflects point across line...works with inverted y-axis
	//line is origin to l
	public static Point2D reflectPointLine(Point2D P, Point2D l)
	{
		double p=P.getX();
		double q=-P.getY();
		
		double a = l.getX();
		double b = -l.getY();
		
		double p2 = (2*a*b*q-p*(b*b-a*a))/(a*a+b*b);
		double q2 = (2*a*b*p+q*(b*b-a*a))/(a*a+b*b);
		
		P.setLocation(p2, -q2);
		
		return P;
	}
	//any line
	public static Point2D reflectPointLine(Point2D P, Line2D l)
	{
		double dX = l.getX1();
		double dY = -l.getY1();
		
		p1.setLocation(P);
		p2.setLocation(l.getP2());
		
		translatePoint(p1, -dX,-dY);
		translatePoint(p2,-dX,-dY);
		
		reflectPointLine(p1,p2);
		
		translatePoint(p1, dX, dY);
		
		P.setLocation(p1);
		
		return P;
	}
	
	//reflects P about P2
	public static Point2D reflectPoint(Point2D P, Point2D P2)
	{
		return P;
	}
	
	public static Point2D intersection(Line2D l1, Line2D l2)
	{
		if (!l1.intersectsLine(l2))
		{return null;}
		
		e1.setEquation(l1);
		e2.setEquation(l2);
		
		p1.setLocation(e1.intersection(e2));
		
		return p1;
	}
	

	//gives a line perpendicular thru p1 to p1-p2
	public static Line2D perpendicular(Point2D P1, Point2D P2)
	{
		double dX = P2.getX()-P1.getX();
		double dY = P1.getY()-P2.getY();
		
		p1.setLocation(P1);
		translatePoint(p1, dY, -dX);
		
		p2.setLocation(P1);
		translatePoint(p2, -dY, dX);
		
		l1.setLine(p1, p2);
		return l1;
	}
	public static void perpendicular(Line2D L, Point2D P1, Point2D P2)
	{
		L.setLine(perpendicular(P1, P2));
	}
	
	//returns two intersections of line and circle as a line
	public static Line2D LineCircleIntersection(Line2D l, Point2D center, double r)
	{
		l1.setLine(l);
		
		TrueTranslate(l1, -center.getX(), -center.getY());
		
		l1 = LineCircleIntersection(l1, r);
		
		TrueTranslate(l1, center.getX(), center.getY());
		
		return l1;
		
		/*
		e1.setEquation(l);
		
		double a = e1.a;
		double b = e1.b;
		double c = e1.c;
		
		double s = a*a+b*b;
		
		double p = center.getX();
		double q = center.getY();
		
		double sqrt = Math.sqrt(s*r*r-a*a*p*p-2*a*(b*q-c)*p-b*b*q*q+2*b*c*q-c*c);
		
		double x = a*(b*q-c)-b*b*p;
		double y = b*(a*p-c)-a*a*q;
		
		double x1 = (x+b*sqrt)/s;
		double x2 = (x-b*sqrt)/s;
		
		double y1 = (y-a*sqrt)/s;
		double y2 = (y+a*sqrt)/s;
		
		l1.setLine(x1, y1, x2, y2);
		
		return l1;
		*/
	}
	//same as above, but circle is at origin
	public static Line2D LineCircleIntersection(Line2D l, double r)
	{
		e1.setEquation(l);
		
		double s = e1.a*e1.a+e1.b*e1.b;
		
		double sqrt = Math.sqrt(s*r*r-e1.c*e1.c);
		
		double x = e1.a*e1.c;
		double y = e1.b*e1.c;
		
		double x1 = (x+e1.b*sqrt)/s;
		double x2 = (x-e1.b*sqrt)/s;
		
		double y1 = (y-e1.a*sqrt)/s;
		double y2 = (y+e1.a*sqrt)/s;
		
		l1.setLine(x1, y1, x2, y2);
		
		return l1;
	}
	
	//returns point closest to l.P1
	public static Point2D ClosestLineCircleIntersection(Line2D l, Point2D center, double r)
	{
		l1 = LineCircleIntersection(l, center, r);
		
		double d1=-1, d2=-1;
		
		if (intersectsLine(l1.getP1(), l))
		{
			d1 = l.getP1().distance(l1.getP1());
		}
		if (intersectsLine(l1.getP2(), l))
		{
			d2 = l.getP1().distance(l1.getP2());
		}
		
		if (d1<0 && d2<0)
		{
			return null;
		}
		
		if (d1<d2 && d1>=0)
		{
			p1.setLocation(l1.getP1());
		}
		else if (d2>=0)
		{
			p1.setLocation(l1.getP2());
		}
		
		return p1;
		
	}
	public static boolean intersectsLine(Point2D p, Line2D l)
	{
		return (l.ptSegDist(p)<.1);
	}
	/*
	public static void SetShortestPath(Line2D l, Point2D P1, Point2D P2, double W, double H)
	{
		
	}
	*/
	
	//
	public static Line2D VectorSubtract(Line2D vector, Line2D other)
	{
		double dX = other.getX2()-other.getX1();
		double dY = other.getY1()-other.getY2();
		
		vector.setLine(vector.getP1(), translatePoint(vector.getP2(), -dX, -dY));
		
		return vector;
	}
	
	//works in regular Cartesian coordinates
	public static void VectorToComponentVector(double Vx, double Vy, double x, double y, Point2D v1, Point2D v2)
	{
		double x1 = Vx;
		double y1 = Vy;
		
		double l1 = (x1*x+y1*y)/(x1*x1+y1*y1);
		
		v1.setLocation(x1*l1, y1*l1);
		
		double x2 = Vy;
		double y2 = -Vx;
		
		double l2 = (x2*x+y2*y)/(x2*x2+y2*y2);
		
		v2.setLocation(x2*l2, y2*l2);
	}
}

//holds variables for ax+by=c
class Equation
{
	//used to hold point of intersection between two lines
	//public static double x, y;
	private static Point2D p;
	static
	{
		p = new Point2D.Double();
	}
	
	double a, b, c;
	
	public void setEquation(double a, double b, double c)
	{
		this.a=a;
		this.b=b;
		this.c=c;
	}

	//inverts y-axis on input
	public void setEquation(Line2D l)
	{
		double x1 = l.getX1();
		double y1 = l.getY1();

		double x2 = l.getX2();
		double y2 = l.getY2();
		
		a = y1-y2;
		b=x2-x1;
		c=y1*x2-x1*y2;
	}
	
	public Point2D intersection(Equation other)
	{
		//sets up equations
		double e = c;
		double c = other.a;
		double d = other.b;
		double f = other.c;
		
		/*
		 * ax+by=e
		 * cx+dy=f
		 * 
		 * ade-abf+abf-bce = e(ad-bc)
		 * cde-cbf+adf-cde = f(ad-bc)
		 */
		
		double det = a*d-b*c;
		
		if (det==0.0) {return null;}//no intersection
		
		double x = (d*e-b*f)/det;
		double y = (a*f-c*e)/det;
		
		p.setLocation(x, y);
		
		return p;
	}
}