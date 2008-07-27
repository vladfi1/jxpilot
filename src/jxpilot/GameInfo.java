package jxpilot;

import java.io.Serializable;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class GameInfo implements Serializable
{
	private AffineTransform identity = new AffineTransform();
	String[][] info;
	
	public GameInfo()
	{
		
	}
	
	public GameInfo(Ship[][] Teams)
	{
		info = new String[Teams.length][];
		for (int t=0;t<Teams.length;t++)
		{
			info[t] = new String[Teams[t].length+1];
		}
	}
	
	public void setInfo(Ship[][] Teams)
	{
		for (int t=0;t<Teams.length;t++)
		{
			info[t][0] = "Team "+(t+1)+"\n";
			
			for (int s =0;s<Teams[t].length;s++)
			{
				if (Teams[t][s]!=null)
					info[t][s+1]=Teams[t][s].toString();
				else
					info[t][s+1]=null;
			}
		}
	}
	
	public void drawInfo(Graphics2D g2, double x, double y)
	{
		g2.setTransform(identity);
		g2.translate(x, y);
		
		double row=0;
		
		for (int s1 = 0;s1<info.length;s1++)
		{
			for (int s2 =0;s2<info[s1].length;s2++)
			{
				if (info[s1][s2]!=null)
				{
					g2.drawString(info[s1][s2], 0.0f, (float)row);
					row += 20;
				}
			}
		}
	}
}
