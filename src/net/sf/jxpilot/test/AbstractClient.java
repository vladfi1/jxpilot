package net.sf.jxpilot.test;

public interface AbstractClient
{
	public void mapInit(BlockMap blockMap);
	public void handleSelf(short x, short y, short vx, short vy, byte heading,
							byte power, byte turnspeed, byte turnresistance,
							short lockId, short lockDist, byte lockDir, byte nextCheckPoint,
							byte currentTank, short fuelSum, short fuelMax,
							short ext_view_width, short ext_view_height,
							byte debris_colors, byte stat, byte autopilot_light);
	public void handleShip(short x, short y, short id, byte dir,
				boolean shield, boolean cloak, boolean emergency_shield, boolean phased, boolean deflector);

	public void handlePlayer(short id, short myTeam, short myChar, String name, String real, String host, ShipShape ship);
	public void handleRadar(int x, int y, int size);
	public void handleLeave(short id);
	public void handleStart(int loops);
	public void handleEnd(int loops);
	public void handleFastShot(int type, ByteBufferWrap in, short num);
	public void handleBall(short x, short y, short id);
	public void handleConnector(short x0,short y0,short x1,short y1, byte tractor);
	
	/**
     * Prints message in client window.
     * 
     * @param message
     *            Message to print.
     */
    public void handleMessage(String message);

}
