package net.sf.jxpilot.test;

import net.sf.jxpilot.util.Holder;

public class FuelHolder implements Holder<FuelHolder>
{
	/**
	 * Unsigned short.
	 */
	int num, fuel;
	
	public int getNum(){return num;}
	public int getFuel(){return fuel;}
	
	public FuelHolder setFuel(int num, int fuel)
	{
		this.num = num;
		this.fuel = fuel;
		return this;
	}
	
	public void set(FuelHolder other)
	{
		other.setFuel(num, fuel);
	}
}
