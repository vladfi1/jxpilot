package net.sf.jxpilot.map;

import static net.sf.jxpilot.game.Base.BaseType;
import static net.sf.jxpilot.game.Cannon.CannonType;
import static net.sf.jxpilot.map.MapBlock.*;

import java.util.*;

import net.sf.jxpilot.game.Base;
import net.sf.jxpilot.game.Cannon;
import net.sf.jxpilot.game.FuelStation;
import net.sf.jxpilot.game.Base.BaseType;
import net.sf.jxpilot.game.Cannon.CannonType;

/**
 * Represents a block-based XPilot map.
 * @author Vlad Firoiu
 */
public class BlockMap implements java.io.Serializable {
	/**
	 * Default UID.
	 */
	private static final long serialVersionUID = 1L;
	
	private final BlockMapSetup setup;
	private final MapBlock[][] blocks;
	/**
	 * Map width and height, measured in blocks*BLOCK_SIZE.
	 */
	private final int width, height;
	
	private ArrayList<Cannon> cannons;
	private ArrayList<Base> bases;
	private ArrayList<FuelStation> fuelStations;
	
	public BlockMap(BlockMapSetup setup) {
		this.setup = setup;
		
		blocks = new MapBlock[setup.getX()][setup.getY()];
		
		for (int x = 0;x<setup.getX();x++) {
			for (int y = 0 ; y<setup.getY();y++) {
				blocks[x][y] = MapBlock.getMapBlock(setup, setup.getNum(x, y));
			}
		}
		
		width = setup.getX() * BLOCK_SIZE;
		height = setup.getY() * BLOCK_SIZE;
		
		MapInit();
	}
	
	
	/**
	 * Initializes non-block map objects, such as cannons.
	 */
	private void MapInit()
	{
		byte[] map_data = setup.getMapData();
		
		//counts game objects
		int num_cannons = 0, num_bases = 0, num_fuels=0;
		for(int i =0;i<map_data.length;i++)
		{
			byte block_type = map_data[i];
			if(CannonType.getCannonType(block_type)!=null) num_cannons++;
			if(BaseType.getBaseType(block_type)!=null) num_bases++;
			if(block_type==BlockMapSetup.SETUP_FUEL) num_fuels++;
		}
		
		//creates game objects
		cannons = new ArrayList<Cannon>(num_cannons);
		num_cannons=0;
		
		bases = new ArrayList<Base>(num_bases);
		num_bases = 0;
		
		fuelStations = new ArrayList<FuelStation>(num_fuels);
		num_fuels = 0;
		
		
		for(int i =0;i<map_data.length;i++)
		{
			byte block_type = map_data[i];
			int x = setup.getX(i);
			int y = setup.getY(i);
			
			Cannon c = Cannon.createCannon(block_type, num_cannons, x, y);
			if(c!=null) {
				cannons.add(num_cannons++, c);
			}
			
			Base b = Base.createBase(block_type, num_bases, x, y);
			if(b!=null) {
				bases.add(num_bases++, b);
			}
			
			if(block_type==BlockMapSetup.SETUP_FUEL) {
				fuelStations.add(num_fuels++, new FuelStation(num_fuels, x, y));
			}
		}
	}
	
	public MapBlock[][] getBlocks(){return blocks;}
	public BlockMapSetup getSetup(){return setup;}
	public MapBlock getBlock(short x, short y){return blocks[x][y];}
	public MapBlock getBlock(int num) {
		return blocks[setup.getX(num)][setup.getY(num)];
	}
	
	public int getWidth(){return width;}
	public int getHeight(){return height;}
	
	public ArrayList<Cannon> getCannons(){return cannons;}
	public ArrayList<Base> getBases(){return bases;}
	public ArrayList<FuelStation> getFuelStations(){return fuelStations;}
	
	@Override
	public String toString() {
		StringBuilder temp = new StringBuilder();
		for (int y=setup.getY()-1;y>=0;y--) {
			for (int x = 0;x<setup.getY();x++) {
				temp.append(blocks[x][y]);
			}
			temp.append('\n');
		}
		return temp.toString();
	}
}