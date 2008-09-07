package net.sf.jxpilot.test;

import net.sf.jxpilot.util.Holder;

public class AbstractDebrisHolder implements Holder<AbstractDebrisHolder>
{
	protected int type;
	/**
	 * Unsigned byte.
	 */
	protected short x,y;
	
	public AbstractDebrisHolder setAbstractDebris(int type, short x, short y)
	{
		this.type = type;
		this.x = x;
		this.y = y;
		return this;
	}
	
	public void set(AbstractDebrisHolder other)
	{
		other.setAbstractDebris(type, x, y);
	}
}
