package net.sf.jxpilot.test;

public class MissileHolder implements Holder<MissileHolder>
{
	protected short x,y;
	protected short len, dir;

	public MissileHolder setMissile(short x, short y,short len, short dir)
	{	
		this.x = x;
		this.y = y;
		this.len = len;
		this.dir = dir;
		return this;
	}

	public void set(MissileHolder other)
	{
		other.setMissile( x,  y,  len,  dir);
	}
	
	public short getX(){return x;}
	public short getY(){return y;}
	public short getLen(){return len;}
	public short getDir(){return dir;}

}
