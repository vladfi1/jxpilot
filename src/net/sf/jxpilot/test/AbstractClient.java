package net.sf.jxpilot.test;

public interface AbstractClient
{
	public void mapInit(BlockMap blockMap);
	public void handleSelf(short x, short y, short vx, short vy, byte heading,
							byte power, byte turnspeed, byte turnresistance,
							short lockId, short lockDist, byte lockDir,
							byte nextCheckPoint, byte autopilotLight,
							byte currentTank, short fuelSum, short fuelMax);
	public void handleShip(short x, short y, short id, byte dir,
				boolean shield, boolean cloak, boolean emergency_shield, boolean phased, boolean deflector);

	public void handlePlayer(short id, short myTeam, short myChar, String name, String real, String host, ShipShape ship);
	public void handleRadar(int x, int y, int size);
	public void handleLeave(short id);
	public void handleStart(int loops);
	public void handleEnd(int loops);
	public void handleFastShot(int type, ByteBufferWrap in, short num);

	/**
     * Prints message in client window.
     * 
     * @param message
     *            Message to print.
     */
    public void handleMessage(String message);

}
