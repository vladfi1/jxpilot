package net.sf.jxpilot;

import net.sf.jxpilot.game.AbstractDebrisHolder;
import net.sf.jxpilot.game.BallHolder;
import net.sf.jxpilot.game.BaseHolder;
import net.sf.jxpilot.game.CannonHolder;
import net.sf.jxpilot.game.ConnectorHolder;
import net.sf.jxpilot.game.FuelHolder;
import net.sf.jxpilot.game.MineHolder;
import net.sf.jxpilot.game.MissileHolder;
import net.sf.jxpilot.game.Player;
import net.sf.jxpilot.game.ScoreObjectHolder;
import net.sf.jxpilot.game.ShipHolder;
import net.sf.jxpilot.map.BlockMap;
import net.sf.xpilotpanel.preferences.Preferences;

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
	public void handleMine(MineHolder m);
	public void handleDebris(AbstractDebrisHolder d);
	public void handleMissile(MissileHolder m);
	public void handleScoreObject(ScoreObjectHolder s);
	public void handleCannon(CannonHolder c);
	public void handleBase(BaseHolder b);
	public void handleFuel(FuelHolder f);
	
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

    /**
     * Preferences of this client.
     * 
     * @see Client#getPreferences()
     */
    public Preferences getPreferences();

}
