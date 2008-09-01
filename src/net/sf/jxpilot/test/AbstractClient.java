package net.sf.jxpilot.test;

public interface AbstractClient
{
	public void handlePacketEnd();
	
	public void mapInit(BlockMap blockMap);
	public void handleSelf(short x, short y, short vx, short vy, byte heading,
							byte power, byte turnspeed, byte turnresistance,
							short lockId, short lockDist, byte lockDir, byte nextCheckPoint,
							byte currentTank, short fuelSum, short fuelMax,
							short ext_view_width, short ext_view_height,
							byte debris_colors, byte stat, byte autopilot_light);
	
	public void handleShip(ShipHolder s);
	public void handlePlayer(Player p);
	public void handleRadar(int x, int y, int size);
	public void handleLeave(short id);
	public void handleStart(int loops);
	public void handleEnd(int loops);
	public void handleFastShot(AbstractDebrisHolder shot);
	public void handleBall(BallHolder b);
	public void handleConnector(ConnectorHolder c);
	public void handleFuel(int num, int fuel);
	public void handleMine(MineHolder m);
	public void handleDebris(AbstractDebrisHolder d);
	public void handleMissile(MissileHolder m);
	public void handleScoreObject(ScoreObjectHolder s);
	
	/**
     * Prints message in client window.
     * 
     * @param message
     *            Message to print.
     */
    public void handleMessage(String message);
    
    /**
     * Returns current view position x value.
     */
    public short getSelfX();
    /**
     * Returns current view position y value.
     */
    public short getSelfY();
    
    public Player getPlayer(short id);
}