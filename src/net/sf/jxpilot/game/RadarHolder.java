package net.sf.jxpilot.game;

import net.sf.jxpilot.util.Holder;

public class RadarHolder implements Holder<RadarHolder>{
	
	/**
	 * Location of detection.
	 */
	protected int x, y,
	/**
	 * Distance from us.
	 */
	size;
	
	public RadarHolder setRadar(int x, int y, int size)
	{
		this.x = x;
		this.y = y;
		this.size = size;
		return this;
	}
	
	public void set(RadarHolder other)
	{
		other.setRadar(x, y, size);
	}
	
	public void setFrom(Holder<RadarHolder> other)
	{
		other.set(this);
	}
	
	public int getX(){return x;}
	public int getY(){return y;}
	public int getSize(){return size;}
	
}
