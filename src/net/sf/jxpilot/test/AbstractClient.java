package net.sf.jxpilot.test;

public interface AbstractClient
{
	public void handleSelf(short x, short y, short vx, short vy, byte heading,
							byte power, byte turnspeed, byte turnresistance,
							short lockId, short lockDist, byte lockDir,
							byte nextCheckPoint, byte autopilotLight,
							byte currentTank, short fuelSum, short fuelMax);
}
