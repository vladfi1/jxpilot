package net.sf.jxpilot;

import net.sf.jxpilot.game.AbstractDebrisHolder;
import net.sf.jxpilot.game.BallHolder;
import net.sf.jxpilot.game.BaseHolder;
import net.sf.jxpilot.game.CannonHolder;
import net.sf.jxpilot.game.ConnectorHolder;
import net.sf.jxpilot.game.FuelHolder;
import net.sf.jxpilot.game.GameWorld;
import net.sf.jxpilot.game.MineHolder;
import net.sf.jxpilot.game.MissileHolder;
import net.sf.jxpilot.game.Player;
import net.sf.jxpilot.game.ScoreObjectHolder;
import net.sf.jxpilot.game.ShipHolder;
import net.sf.jxpilot.graphics.JXPilotFrame;
import net.sf.jxpilot.map.BlockMap;
import net.sf.jxpilot.net.NetClient;
import net.sf.jxpilot.util.BitVector;
import net.sf.xpilotpanel.preferences.Preferences;

public class Client implements AbstractClient, ClientInputListener
{
	private volatile boolean quit=false;
	
	private NetClient netClient;
	private BlockMap blockMap;
	private JXPilotFrame frame;
	private BitVector keyboard;

	/**
     * "Preferences" sent by XPilotPanel.<br>
     * For now its only copy of XPilotPanel's preferences.
     */
    private Preferences prefs = null;

	private GameWorld world;
	
	/**
	 * Our current position. Perhaps would be more efficient to be protected so as to allow direct access.
	 */
	private short selfX, selfY;
	
	public short getSelfX(){return selfX;}
	public short getSelfY(){return selfY;}
	
	/**
	 * The id of the player we are watching, or -1 if we aren't dead.
	 */
	private short eyesId = -1;
	
	public Player getPlayer(short id)
	{
		return world.getPlayer(id);
	}
	
	/**
     * Creates new <code>Client</code>.
     * 
     * @param p
     *            Preferences for this client or null for launch w/o
     *            XPilotPanel.
     */
    public Client(Preferences p) {
        prefs = p;
        netClient = new NetClient(this);
        keyboard = netClient.getKeyboard();
    }

	public void runClient(String serverIP, int serverPort)
	{
		netClient.runClient(serverIP, serverPort);
	}
	
	public BlockMap getMap(){return blockMap;}
	
	//Abstract Client methods
	
	public void handlePacketEnd()
	{
		//frame.activeRender();
	}
	
	/**
	 * Initializes world which maintains map and various drawables.
	 * Also initializes JXPilotFrame.
	 */
	public void mapInit(BlockMap blockMap)
	{
		world = new GameWorld(blockMap);
		
		this.blockMap = blockMap;
		frame = new JXPilotFrame(world, this);
		//frame.setDrawables(drawables);
		
		frame.setVisible(true);
	}
	
	public void handleSelf(short x, short y, short vx, short vy, byte heading,
			byte power, byte turnspeed, byte turnresistance,
			short lockId, short lockDist, byte lockDir, byte nextCheckPoint,
			byte currentTank, short fuelSum, short fuelMax,
			short ext_view_width, short ext_view_height,
			byte debris_colors, byte stat, byte autopilot_light)
	{
		eyesId = -1;
		selfX = x;
		selfY = y;
		
		world.setViewPosition(x, y);
	}
	
	public void handleShip(ShipHolder s)
	{
		world.addShip(s);
	}
	
	public void handlePlayer(Player p)
	{
		world.addPlayer(p);
	}
	
	public void handleRadar(int x, int y, int size)
	{
		
	}
	
	public void handleLeave(short id)
	{
		world.removePlayer(id);
	}
	
	public void handleStart(int loops)
	{
		world.update();
	}
	
	public void handleEyes(short id)
	{
		eyesId = id;
		world.setViewPosition(eyesId);
	}
	
	public void handleScoreObject(ScoreObjectHolder s)
	{
		world.addScoreObject(s);
	}
	
	public void handleEnd(int loops)
	{
		//setFrameView();
		if(!quit)
		frame.activeRender();
	}
	
	public void handleFastShot(AbstractDebrisHolder shot)
	{
		//shotHandler.addDrawable(s);
		
		world.addFastShot(shot);
	}

	
	public void handleBall(BallHolder ball)
	{
		//ballHandler.addDrawable(b);
		
		world.addBall(ball);
	}
	
	public void handleConnector(ConnectorHolder connector)
	{
		//connectorHandler.addDrawable((Holder)connector);
		
		world.addConnector(connector);
	}
	
	public void handleFuel(FuelHolder f)
	{
		world.handleFuel(f);
	}
	
	public void handleCannon(CannonHolder c)
	{
		world.handleCannon(c);
	}
	
	public void handleBase(BaseHolder b)
	{
		world.handleBase(b);
	}
	
    /**
     * @see net.sf.jxpilot.AbstractClient#handleMessage(java.lang.String)
     */
    @Override
    public void handleMessage(String message) {
        frame.getMessagePool().publishMessage(message);
    }
	
	public void handleMine(MineHolder mine)
	{
		//mineHandler.addDrawable(m);
		
		world.addMine(mine);
	}
	public void handleDebris(AbstractDebrisHolder debris)
	{
		//debrisHandler.addDrawable(d);
		world.addSpark(debris);
		
		//System.out.println("NetClient handling debris!");
	}
	
	public void handleMissile(MissileHolder missile)
	{
		//missileHandler.addDrawable(m);
		world.addMissile(missile);
	}
	
	//Client Input Listener methods
	public void quit()
	{
		quit=true;
		netClient.quit();
		frame.finish();
		//System.exit(0);
	}
	
	public void setKey(int key, boolean value)
	{
		synchronized(keyboard)
		{
			keyboard.setBit(key, value);
		}
	}
	
	public void movePointer(short amount)
	{
		netClient.movePointer(amount);
	}

	public void talk(String message)
	{
		netClient.netTalk(message);
	}
	
    /**
     * Returns preferences of this client or <code>null</code>, if it was
     * launched w/o XPilotPanel.
     * 
     * @return Client's preferences.
     */
    public Preferences getPreferences() {
        return prefs;
    }
    
}