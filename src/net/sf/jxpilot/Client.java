package net.sf.jxpilot;

import javax.swing.JOptionPane;

import net.sf.jxpilot.game.*;
import net.sf.jxpilot.graphics.*;
import net.sf.jxpilot.map.BlockMap;
import net.sf.jxpilot.net.NetClient;
import net.sf.jxpilot.net.PlayerPacket;
import net.sf.jxpilot.net.QuitPacket;
import net.sf.jxpilot.net.ShutdownPacket;
import net.sf.jxpilot.util.BitVector;
import net.sf.jgamelibrary.preferences.Preferences;

public class Client implements AbstractClient, ClientInputListener
{
	private volatile boolean quit=false;
	
	private NetClient netClient;
	private BlockMap blockMap;
	private JXPilotFrame frame;

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
	 * The Player object representing us.
	 */
	private Player self;
	
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
    }

	public void runClient(String serverIP, int serverPort)
	{
		netClient.runClient(serverIP, serverPort);
	}
	
	public BlockMap getMap(){return blockMap;}
	public GameWorld getWorld(){return world;}
	public JXPilotFrame getFrame(){return frame;}
	public HUD getHUD(){return hud;}
	public Player getSelf(){return self;}
	
	//Abstract Client methods
	@Override
	public void handlePacketEnd() {
		//frame.activeRender();
	}
	
	@Override
	public void handleTimeout() {
		JOptionPane.showMessageDialog(frame, "Server timed out!");
		quit();
	}
	
	@Override
	public void handleShutdown(ShutdownPacket s) {
		JOptionPane.showMessageDialog(frame, "Server shut down!");
		quit();
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
		
		DisplayMode mode = prefs!=null ?
				DisplayMode.getDisplayMode(prefs.get(DisplayMode.displayModeKey))
				: DisplayMode.FSEM;
		
		frame = new JXPilotFrame(mode, world, this);
		//frame.setDrawables(drawables);
		
		frame.setVisible(true);
	}
	
	public void handleQuit(QuitPacket q) {
		JOptionPane.showMessageDialog(frame, "Server closed connection:\n" + q.getReason());
		this.quit();
	}
	
	public void handleSelf(SelfHolder self) {
		eyesId = -1;
		selfX = self.getX();
		selfY = self.getY();
		
		world.setViewPosition(selfX, selfY);
		world.setExtView(self.getExtViewWidth(), self.getExtViewHeight());

		hud.setSelf(self);
	}
	
	public void handleShip(ShipHolder s)
	{
		world.addShip(s);
	}
	
	public void handlePlayer(PlayerPacket p) {
		Player player = new Player(p.getId(), p.getTeam(), p.getChar(), p.getName(), p.getReal(), p.getHost(), p.getShipShape());
		
		if(player.getNick().equalsIgnoreCase(netClient.getNick()))
		{
			self = player;
			world.setSelf(player);
			System.out.println("Found self!");
		}
		
		world.addPlayer(player);
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
	
	/**
	 * The amount of degrees (in 128 degree mode) that the ship moves is amount*turnspeed/64.
	 */
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