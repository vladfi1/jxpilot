package net.sf.jxpilot;

import net.sf.jxpilot.game.*;
import net.sf.jxpilot.graphics.*;
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
	private HUD hud;
	
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
	public GameWorld getWorld(){return world;}
	
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
		hud = world.getHud();
		this.blockMap = blockMap;
		
		DisplayMode mode = DisplayMode.getDisplayMode(prefs.get(DisplayMode.displayModeKey));
		
		frame = new JXPilotFrame(mode, world, this);
		//frame.setDrawables(drawables);
		
		frame.setVisible(true);
	}
	
	public void handleQuit(String reason)
	{
		this.quit();
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
		world.setExtView(ext_view_width, ext_view_height);
		
		hud.setLockId(lockId);
	}
	
	public void handleShip(ShipHolder s)
	{
		world.addShip(s);
	}
	
	public void handlePlayer(Player p)
	{
		world.addPlayer(p);
	}
	
	public void handleRadar(RadarHolder r)
	{
		world.handleRadar(r);
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
	
	public void handleScore(short id, short score, short life)
	{
		Player p = world.getPlayer(id);
		if(p!=null)
		{
			p.setScore(score);
			p.setLife(life);
		}
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
		netClient.setKey(key, value);
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