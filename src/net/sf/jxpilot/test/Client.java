package net.sf.jxpilot.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.*;

public class Client implements AbstractClient, ClientInputListener
{
	public static final int MAX_SHIPS=100;
	
	private NetClient netClient;
	private BlockMap blockMap;
	private JXPilotFrame frame;
	private Vector<Collection<? extends Drawable>> drawables;
	private Collection<DrawableHandler<? extends ExtendedDrawable<?>>> drawableHandlers;
	private BitVector keyboard;
	
	//Collections holding drawables
	private HashMap<Short, Player> playerMap = new HashMap<Short, Player>();
	private DrawableHandler<FastShot> shotHandler;
	private final int SHOTS_SIZE = 300;
	private DrawableHandler<Connector> connectorHandler;
	private final int CONNECTORS_SIZE = 10;
	private DrawableHandler<Ball> ballHandler;
	private final int BALLS_SIZE = 10;
	private DrawableHandler<Mine> mineHandler;
	private final int MINES_SIZE = 20;
	private DrawableHandler<Debris> debrisHandler;
	private final int DEBRIS_SIZE = 200;
	private DrawableHandler<Missile> missileHandler;
	private final int MISSILE_SIZE = 20;
	
	/**
	 * Our current position. Perhaps would be more efficient to be protected so as to allow direct access.
	 */
	private short selfX, selfY;
	
	public short getSelfX(){return selfX;}
	public short getSelfY(){return selfY;}
	
	/**
	 * The id of the player we are watching, or -1 if we aren't dead.
	 */
	private int eyesId = -1;
	
	public Player getPlayer(short id)
	{
		return playerMap.get(id);
	}
	
	public Client()
	{
		netClient = new NetClient(this);
		keyboard = netClient.getKeyboard();
		
		drawables = new Vector<Collection<? extends Drawable>>();
		drawables.add(playerMap.values());
		
		initDrawableHandlers();
	}
	
	private void initDrawableHandlers()
	{
		drawableHandlers = new ArrayList<DrawableHandler<? extends ExtendedDrawable<?>>>();
		
		shotHandler = new DrawableHandler<FastShot>(new FastShot(this), SHOTS_SIZE);
		drawableHandlers.add(shotHandler);
		
		ballHandler = new DrawableHandler<Ball>(new Ball(this), BALLS_SIZE);
		drawableHandlers.add(ballHandler);
		
		connectorHandler = new DrawableHandler<Connector>(new Connector(), CONNECTORS_SIZE);
		drawableHandlers.add(connectorHandler);
		
		mineHandler = new DrawableHandler<Mine>(new Mine(), MINES_SIZE);
		drawableHandlers.add(mineHandler);
		
		debrisHandler = new DrawableHandler<Debris>(new Debris(this), DEBRIS_SIZE);
		drawableHandlers.add(debrisHandler);
		
		missileHandler = new DrawableHandler<Missile>(new Missile(), MISSILE_SIZE);
		drawableHandlers.add(missileHandler);
		
		drawables.addAll(drawableHandlers);
	}
	
	public void runClient(String serverIP, int serverPort)
	{
		netClient.runClient(serverIP, serverPort);
	}
	
	public BlockMap getMap(){return blockMap;}
	
	//Abstract Client methods
	public void mapInit(BlockMap blockMap)
	{
		this.blockMap = blockMap;
		frame = new JXPilotFrame(blockMap, this);
		frame.setDrawables(drawables);
		frame.setVisible(true);
	}
	
	private void setFrameView()
	{
		if (eyesId == -1)
		frame.setView((double)selfX/MapBlock.BLOCK_SIZE, (double)selfY/MapBlock.BLOCK_SIZE);
		else
		{
			Player p = playerMap.get(eyesId);
			frame.setView(p.getX()/MapBlock.BLOCK_SIZE, p.getY()/MapBlock.BLOCK_SIZE);
		}
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
		//shots.setSelfPosition(x, y);
		//setFrameView();
	}
	
	public void handleShip(short x, short y, short id, byte dir,
				boolean shield, boolean cloak, boolean emergency_shield, boolean phased, boolean deflector)
	{
		Player p = playerMap.get(id);
		
		if (p==null)
		{
			System.out.println("********No ship matches id = " + id + "*********");
		}
		else
		{
			p.setShip(x, y, dir, shield, cloak, emergency_shield, phased, deflector);
			p.setActive(true);
		}
	}
	
	public void handlePlayer(short id, short my_team, short my_char, String nick, String real, String host, ShipShape ship)
	{
		//ships[id] = new Ship(ship, id);
		//drawables.add(ships[id]);
		Player p = new Player(id, my_team, my_char, nick, real, host, ship);
		playerMap.put(id, p);
		//frame.repaint();
	}
	
	public void handleRadar(int x, int y, int size)
	{
		
	}
	
	public void handleLeave(short id)
	{
		playerMap.remove(id);
	}
	
	public void handleStart(int loops)
	{
		clearHandlers();
	}
	
	public void handleEyes(int id)
	{
		eyesId = id;
	}
	
	/**
	 * Clears all in-game objects so that they are refreshed each frame.
	 */
	private void clearHandlers()
	{
		for (Player p : playerMap.values())
		{
			p.setActive(false);
		}
		
		//shots.clearShots();		
		for (DrawableHandler<?> d : drawableHandlers)
		{
			d.clearDrawables();
		}
	}
	
	public void handleEnd(int loops)
	{
		setFrameView();
		frame.activeRender();
	}
	
	public void handleFastShot(FastShot s)
	{
		shotHandler.addDrawable(s);
		//in.position(in.position()+2*num);
	}

	
	public void handleBall(Ball b)
	{
		ballHandler.addDrawable(b);
	}
	
	public void handleConnector(Connector c)
	{
		connectorHandler.addDrawable(c);
	}
	
	public void handleFuel(int num, int fuel)
	{
		
	}
	
	public void handleMine(Mine m)
	{
		mineHandler.addDrawable(m);
	}
	public void handleDebris(Debris d)
	{
		debrisHandler.addDrawable(d);
	}
	
	public void handleMissile(Missile m)
	{
		missileHandler.addDrawable(m);
	}
	
	//Client Input Listener methods
	public void quit()
	{
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

    /**
     * @see net.sf.jxpilot.test.AbstractClient#handleMessage(java.lang.String)
     */
    @Override
    public void handleMessage(String message) {
        frame.getMessagePool().publishMessage(message);
    }
}