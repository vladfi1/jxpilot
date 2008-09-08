package net.sf.jxpilot.game;

import net.sf.jxpilot.util.Holder;

/**
 * Carries information server sends for bases.
 * @author vlad
 */
public class BaseHolder implements Holder<BaseHolder>
{
	/**
	 * Id of player that has taken base.
	 */
	protected short id;
	/**
	 * Base number, as appears in map data.
	 */
	int num;
	
	public BaseHolder setBase(short id, int num)
	{
		this.id = id;
		this.num = num;
		
		return this;
	}
	
	public void set(BaseHolder other)
	{
		other.setBase(id, num);
	}
	
	public short getId(){return id;}
	public int getNum(){return num;}
}
