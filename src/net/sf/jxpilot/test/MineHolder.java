package net.sf.jxpilot.test;

public class MineHolder implements Holder<MineHolder>{

	protected short x,y,id;
	protected byte team_mine;

	public MineHolder setMine(short x, short y, byte team_mine, short id)
	{
		this.x = x;
		this.y = y;
		this.team_mine = team_mine;
		this.id = id;
		return this;
	}

	public void set(MineHolder other)
	{
		other.setMine( x,  y,  team_mine,  id);
	}
	
	public short getX(){return x;}
	public short getY(){return y;}
	public byte getTeamMine(){return team_mine;}
	public short getId(){return id;}
}
