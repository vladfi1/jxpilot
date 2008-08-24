package net.sf.jxpilot.test;

import java.util.*;

public class Client implements AbstractClient, ClientInputListener
{
	public static final int MAX_SHIPS=100;
	
	private NetClient netClient;
	private Map map;
	private MapFrame frame;
	private Vector<Collection<? extends Drawable>> drawables;
	
	//private Ship[] ships = new Ship[MAX_SHIPS];
	private HashMap<Integer, Ship> shipMap = new HashMap<Integer, Ship>();
	
	public Client()
	{
		netClient = new NetClient(this);
		
		drawables = new Vector<Collection<? extends Drawable>>();
		drawables.add(shipMap.values());
	}
	
	public void runClient(String serverIP, int serverPort)
	{
		netClient.runClient(serverIP, serverPort);
	}
	
	//Abstract Client methods
	public void mapInit(Map map)
	{
		this.map = map;
		frame = new MapFrame(map, this);
		frame.setDrawables(drawables);
		frame.setVisible(true);
	}
	
	public void handleSelf(short x, short y, short vx, short vy, byte heading,
			byte power, byte turnspeed, byte turnresistance,
			short lockId, short lockDist, byte lockDir,
			byte nextCheckPoint, byte autopilotLight,
			byte currentTank, short fuelSum, short fuelMax)
	{
		frame.setView((double)x/MapBlock.BLOCK_SIZE, (double)y/MapBlock.BLOCK_SIZE);
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
			s.setShip(x, y, dir);
		}
	}
	
	public void handlePlayer(short id, short myTeam, short myChar, String name, String real, String host, ShipShape ship)
	{
		//ships[id] = new Ship(ship, id);
		//drawables.add(ships[id]);
		Ship s = new Ship(ship, id);
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
	
	public void handleEnd()
	{
		frame.repaint();
	}
	
	//Client Input Listener methods
	public void quit()
	{
		netClient.sendQuit();
		System.exit(0);
	}
}