package net.sf.jxpilot.test;

import java.util.*;

public class Client implements AbstractClient, ClientInputListener
{
	public static final int MAX_SHIPS=100;
	
	private NetClient netClient;
	private BlockMap blockMap;
	private JXPilotFrame frame;
	private Vector<Collection<? extends Drawable>> drawables;
	private Collection<DrawableHandler<?>> drawableHandlers;
	private BitVector keyboard;
	
	//Collections holding drawables
	private HashMap<Integer, Ship> shipMap = new HashMap<Integer, Ship>();
	private ShotHandler shots;
	private DrawableHandler<Connector> connectorHandler;
	private final int CONNECTORS_SIZE = 10;
	private DrawableHandler<Ball> ballHandler;
	private final int BALLS_SIZE = 10;
	private DrawableHandler<Mine> mineHandler;
	private final int MINES_SIZE = 20;
	
	/**
	 * Our current position.
	 */
	private short selfX, selfY;
	
	
	public Client()
	{
		netClient = new NetClient(this);
		keyboard = netClient.getKeyboard();
		
		drawables = new Vector<Collection<? extends Drawable>>();
		drawables.add(shipMap.values());
		

		shots = new ShotHandler();
		drawables.add(shots);
		
		initDrawableHandlers();
	}
	
	private void initDrawableHandlers()
	{
		drawableHandlers = new ArrayList<DrawableHandler<?>>();
		
		ballHandler = new DrawableHandler<Ball>(new Ball(), BALLS_SIZE);
		drawableHandlers.add(ballHandler);
		
		connectorHandler = new DrawableHandler<Connector>(new Connector(), CONNECTORS_SIZE);
		drawableHandlers.add(connectorHandler);
		
		mineHandler = new DrawableHandler<Mine>(new Mine(), MINES_SIZE);
		drawableHandlers.add(mineHandler);
		
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
		frame.setView((double)selfX/MapBlock.BLOCK_SIZE, (double)selfY/MapBlock.BLOCK_SIZE);
	}
	
	public void handleSelf(short x, short y, short vx, short vy, byte heading,
			byte power, byte turnspeed, byte turnresistance,
			short lockId, short lockDist, byte lockDir, byte nextCheckPoint,
			byte currentTank, short fuelSum, short fuelMax,
			short ext_view_width, short ext_view_height,
			byte debris_colors, byte stat, byte autopilot_light)
	{
		selfX = x;
		selfY = y;
		shots.setSelfPosition(x, y);
		setFrameView();
	}
	
	public void handleShip(short x, short y, short id, byte dir,
				boolean shield, boolean cloak, boolean emergency_shield, boolean phased, boolean deflector)
	{
		Ship s = shipMap.get((int)id);
		
		if (s==null)
		{
			System.out.println("********No ship matches id = " + id + "*********");
		}
		else
		{
			s.setShip(x, y, dir, shield, cloak, emergency_shield, phased, deflector);
			s.setActive(true);
		}
	}
	
	public void handlePlayer(short id, short myTeam, short myChar, String name, String real, String host, ShipShape ship)
	{
		//ships[id] = new Ship(ship, id);
		//drawables.add(ships[id]);
		Ship s = new Ship(ship, id, name);
		shipMap.put((int)id, s);
		//frame.repaint();
	}
	
	public void handleRadar(int x, int y, int size)
	{
		
	}
	
	public void handleLeave(short id)
	{
		shipMap.remove(id);
	}
	
	public void handleStart(int loops)
	{
		clearHandlers();
	}
	
	/**
	 * Clears all in-game objects so that they are refreshed each frame.
	 */
	private void clearHandlers()
	{
		for (Ship s : shipMap.values())
		{
			s.setActive(false);
		}
		
		shots.clearShots();
		
		for (DrawableHandler<?> d : drawableHandlers)
		{
			d.clearDrawables();
		}
	}
	
	public void handleEnd(int loops)
	{
		frame.activeRender();
	}
	
	public void handleFastShot(int type, ByteBufferWrap in, short num)
	{
		System.out.println("\nFastShot type = " + type + "\nnum = " + num);
		
		for (int i = 0;i<num;i++)
		{
			shots.addShot(type, in.getUnsignedByte(), in.getUnsignedByte());
		}
		
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
