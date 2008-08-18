package net.sf.jxpilot.test;

import java.util.*;

public class Client implements AbstractClient
{
	public static final int MAX_SHIPS=100;
	
	private MapFrame frame;
	private List<Drawable> drawables;
	
	private Ship[] ships = new Ship[MAX_SHIPS];
	
	
	public Client(MapFrame frame)
	{
		drawables = new LinkedList<Drawable>();
		this.frame = frame;
		frame.setDrawables(drawables);
	}
	
	
	public void handleSelf(short x, short y, short vx, short vy, byte heading,
			byte power, byte turnspeed, byte turnresistance,
			short lockId, short lockDist, byte lockDir,
			byte nextCheckPoint, byte autopilotLight,
			byte currentTank, short fuelSum, short fuelMax)
	{
		frame.setView((double)x/MapBlock.BLOCK_SIZE, (double)y/MapBlock.BLOCK_SIZE);
		frame.repaint();
	}
	
	public void handleShip(short x, short y, short id, byte dir,
				boolean shield, boolean cloak, boolean emergency_shield, boolean phased, boolean deflector)
	{
		ships[id].setShip(x, y, dir);
	}
	
	public void handlePlayer(short id, short myTeam, short myChar, String name, String real, String host, ShipShape ship)
	{
		ships[id] = new Ship(ship, id);
		drawables.add(ships[id]);
		frame.repaint();
	}
}