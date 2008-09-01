package net.sf.jxpilot.test;

public class ScoreObjectHolder implements Holder<ScoreObjectHolder>
{
	protected short score;
	/**
	 * Unsigned shorts.
	 */
	protected int x,y;
	protected String message;
	
	public ScoreObjectHolder setScoreObject(short score, int x, int y, String message)
	{
		this.score = score;
		this.x = x;
		this.y = y;
		this.message= message;
		return this;
	}
	
	@Override
	public void set(ScoreObjectHolder other)
	{
		other.setScoreObject(score, x, y, message);
	}
	
	public short getScore(){return score;}
	public int getX(){return x;}
	public int getY(){return y;}
	public String getMessage(){return message;};
}
