package net.sf.jxpilot.test;

public class Client implements AbstractClient
{
	private MapFrame frame;
	
	public Client(MapFrame frame)
	{
		this.frame = frame;
	}
	
	public void handleSelf(short x, short y, short vx, short vy, byte heading,
			byte power, byte turnspeed, byte turnresistance,
			short lockId, short lockDist, byte lockDir,
			byte nextCheckPoint, byte autopilotLight,
			byte currentTank, short fuelSum, short fuelMax)
	{
		frame.setView((double)x/MapBlock.BLOCK_SIZE, (double)y/MapBlock.BLOCK_SIZE);
	}
	
	
}
