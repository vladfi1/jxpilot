package net.sf.jxpilot.game;

import static net.sf.jxpilot.map.MapBlock.BLOCK_SIZE;
import static net.sf.jxpilot.util.Utilities.getAngleFrom128;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;

import net.sf.jxpilot.graphics.Drawable;
import net.sf.jxpilot.map.*;
import net.sf.jxpilot.util.*;

/**
 * Class that manages the various objects in the world.
 * All drawable objects are contained in here.
 * @author vlad
 */
public class GameWorld implements Drawable{
	
	private BlockMap map;
	private BlockMapSetup setup;
	
	private LinkedList<Drawable> drawableList;
	private Vector<Iterable<? extends Drawable>> drawables;
	
	private Collection<HolderList<?, ? extends Drawable>> holderLists;
	
	//various collections to handle 
	/**
	 * Map holding the players by id number.
	 */
	private HashMap<Short, Player> playerMap = new HashMap<Short, Player>();
	private HolderList<ShipHolder, Ship> shipHandler;
	private final int SHIPS_SIZE = 10;
	private HolderList<AbstractDebrisHolder, FastShot> shotHandler;
	private final int SHOTS_SIZE = 300;
	private HolderList<ConnectorHolder, Connector> connectorHandler;
	private final int CONNECTORS_SIZE = 10;
	private HolderList<BallHolder, Ball> ballHandler;
	private final int BALLS_SIZE = 10;
	private HolderList<MineHolder, Mine> mineHandler;
	private final int MINES_SIZE = 20;
	private HolderList<AbstractDebrisHolder, Spark> sparkHandler;
	private final int DEBRIS_SIZE = 200;
	private HolderList<MissileHolder, Missile> missileHandler;
	private final int MISSILE_SIZE = 20;

	private final ArrayList<Cannon> cannons;
	private final ArrayList<DrawableBase> bases;
	private final ArrayList<FuelStation> fuelStations;
	
	private HUD hud;
	
	/**
	 * The current view position in XPilot pixels.
	 */
	private short viewX, viewY;
	
	/**
	 * View dimensions.
	 */
	private short ext_view_width, ext_view_height;
	
	public GameWorld(BlockMap map)
	{
		this.map = map;
		this.setup = map.getSetup();
		
		drawables = new Vector<Iterable<? extends Drawable>>();
		
		cannons = map.getCannons();
		drawables.add(cannons);	

		fuelStations = map.getFuelStations();
		drawables.add(fuelStations);
		
		ArrayList<Base> mapBases = map.getBases();
		bases = new ArrayList<DrawableBase>(mapBases.size());
		for(int i = 0;i<mapBases.size();i++)
		{
			bases.add(i, new DrawableBase(mapBases.get(i)));
		}
		drawables.add(bases);
		
		hud = new HUD(this);
		//drawables.add(hud.getRadarHandler());
		//drawables.add(hud.getScoreObjectHandler());
		
		initDrawableHandlers();
		
		drawableList = new LinkedList<Drawable>();
		drawableList.add(this);
		drawableList.add(hud);
	}
	
	private void initDrawableHandlers()
	{
		holderLists = new ArrayList<HolderList<?, ? extends Drawable>>();

		shotHandler = new HolderList<AbstractDebrisHolder, FastShot>(fastShotFactory, SHOTS_SIZE);
		holderLists.add(shotHandler);
		
		ballHandler = new HolderList<BallHolder, Ball>(ballFactory, BALLS_SIZE);
		holderLists.add(ballHandler);
		
		connectorHandler = new HolderList<ConnectorHolder, Connector>(connectorFactory, CONNECTORS_SIZE);
		holderLists.add(connectorHandler);
		
		mineHandler = new HolderList<MineHolder, Mine>(mineFactory, MINES_SIZE);
		holderLists.add(mineHandler);
		
		sparkHandler = new HolderList<AbstractDebrisHolder, Spark>(sparkFactory, DEBRIS_SIZE);
		holderLists.add(sparkHandler);
		
		missileHandler = new HolderList<MissileHolder, Missile>(missileFactory, MISSILE_SIZE);
		holderLists.add(missileHandler);
		
		//scoreObjectHandler = new TimedQueue<ScoreObject>(SCORE_OBJECT_DURATION);
		//drawables.add(scoreObjectHandler);
		//holderLists.add(scoreObjectHandler);
		
		shipHandler = new HolderList<ShipHolder, Ship>(shipFactory, SHIPS_SIZE);
		holderLists.add(shipHandler);
		
		drawables.addAll(holderLists);
	}
	
	public BlockMap getMap(){return map;}
	
	public short getSelfX(){return viewX;}
	public short getSelfY(){return viewY;}
	public void setViewPosition(short x, short y)
	{
		viewX = x;
		viewY = y;
	}
	
	public void setViewPosition(short eyesId)
	{
		Player p = getPlayer(eyesId);
		
		if (p==null)
		{
			System.out.println("\nNo player matches id = " + eyesId);
		}
		else
		{
			viewX = p.getShip().getX();
			viewY = p.getShip().getY();
		}
	}
	
	public void setExtView(short ext_view_width, short ext_view_height)
	{
		this.ext_view_height = 	ext_view_height;
		this.ext_view_width = ext_view_width;
	}
	
	public short getExtViewWidth(){return ext_view_width;}
	public short getExtViewHeight(){return ext_view_height;}
	
	public ArrayList<Ship> getShipHandler(){return shipHandler;}
	public ArrayList<Ball> getBallHandler(){return ballHandler;}
	public ArrayList<Connector> getConnectorHandler(){return connectorHandler;}
	public ArrayList<Mine> getMineHandler(){return mineHandler;}
	public ArrayList<Missile> getMissileHandler(){return missileHandler;}
	public ArrayList<FastShot> getShotHandler(){return shotHandler;}
	public ArrayList<Spark> getSparkHandler(){return sparkHandler;}
	
	public Vector<Iterable<? extends Drawable>> getAllDrawables(){return drawables;}
	public HUD getHud(){return hud;}
	
	public Iterable<? extends Drawable> getDrawables()
	{
		return drawableList;
	}
	
	/**
	 * Paints all objects in the world. Does NOT paint the HUD.
	 */
	@Override
	public void paintDrawable(Graphics2D g2d)
	{
		AffineTransform saved = g2d.getTransform();
		
		for(Iterable<? extends Drawable> i: drawables)
		{
			for(Drawable d : i)
			{
				d.paintDrawable(g2d);
				g2d.setTransform(saved);
			}
		}
	}
	
	//methods for manipulating game objects
	public Player getPlayer(short id)
	{
		return playerMap.get(id);
	}
	public void addPlayer(Player p)
	{
		playerMap.put(p.getId(), p);
	}
	public Player removePlayer(short id)
	{
		return playerMap.remove(id);
	}
	public Player removePlayer(Player p)
	{
		return playerMap.remove(p.getId());
	}
	
	public Collection<Player> getPlayers()
	{
		return playerMap.values();
	}
	
	public void addShip(ShipHolder s)
	{
		Player p = getPlayer(s.getId());
		
		if (p==null)
		{
			System.out.println("No player matches id = " + s.getId());
			return;
		}
		
		p.setShip(s);
		
		shipHandler.add(s);
	}	
	
	public void addBall(BallHolder ball)
	{
		ballHandler.add(ball);
	}
	
	public void addConnector(ConnectorHolder connector)
	{
		connectorHandler.add(connector);
	}
	
	public void addMine(MineHolder mine)
	{
		mineHandler.add(mine);
	}
	
	public void addMissile(MissileHolder missile)
	{
		missileHandler.add(missile);
	}
	
	public void addFastShot(AbstractDebrisHolder shot)
	{
		shotHandler.add(shot);
	}
	
	public void addSpark(AbstractDebrisHolder spark)
	{
		sparkHandler.add(spark);
	}
	
	public void addScoreObject(ScoreObjectHolder s)
	{
		//scoreObjectHandler.add(new ScoreObject(s));
		hud.addScoreObject(s);
		//System.out.println("Adding score object in GameWorld");
	}
	
	public void handleCannon(CannonHolder c)
	{
		c.set(cannons.get(c.getNum()));
	}
	
	public void handleBase(BaseHolder b)
	{
		for(Base base : bases)
		{
			if(base.getId()==b.getId())
				base.leave();
		}
		
		//b.set(bases.get(b.getNum()));
		
		bases.get(b.getNum()).setFrom(b);
	}
	
	public void handleFuel(FuelHolder f)
	{
		f.set(fuelStations.get(f.getNum()));
	}
	
	public void handleRadar(RadarHolder r)
	{
		hud.addRadar(r);
	}
	
	/**
	 * Clears all in-game objects so that they are refreshed each frame.
	 */
	public void update()
	{
		for (Player p : playerMap.values())
		{
			p.setActive(false);
		}
		
		//shots.clearShots();		
		for (HolderList<?,?> d : holderLists)
		{
			d.clear();
		}

		hud.update();
	}
	
	//inner Drawable classes	
	protected class Ship extends ShipHolder implements Drawable
	{
		public static final int SHIP_RADIUS = 16;
		private  final Color SHIP_COLOR = Color.WHITE;
		
		private Ellipse2D shieldShape;		
		private Player player;
		
		public Ship()
		{
			shieldShape = new Ellipse2D.Float();
			shieldShape.setFrame(-SHIP_RADIUS, -SHIP_RADIUS, 2*SHIP_RADIUS, 2*SHIP_RADIUS);
		}
		
		@Override
		public void setFrom(Holder<ShipHolder> other)
		{
			super.setFrom(other);
			player = getPlayer(super.id);
		}
		
		public void paintDrawable(Graphics2D g2d)
		{
			g2d.setColor(SHIP_COLOR);
			
			int x = Utilities.wrap(map.getWidth(), viewX, super.x);
			int y = Utilities.wrap(map.getHeight(), viewY, super.y);
			
			g2d.translate(x, y);
			
			//need to flip g2d so nick comes out ok
			//g2d.scale(1, -1);
			//g2d.drawString(nick, (float)-bounds.getWidth()/2, SHIP_RADIUS + (float)bounds.getHeight()/2);
			//g2d.scale(1, -1);
			
			Utilities.drawAdjustedStringDown(g2d, player.getNick(), 0, -SHIP_RADIUS);
			
			if (shield)
			{
				g2d.draw(shieldShape);
			}
			
			g2d.rotate(getAngleFrom128(heading));
			g2d.draw(player.getShape());
			//g2d.rotate(-heading);
			//g2d.translate(-x,-y);
			//g2d.rotate(-heading, -x, -y);
			//g2d.setTransform(saved);
		}
	}
	public final Factory<Ship> shipFactory = new Factory<Ship>(){
		public Ship newInstance(){return new Ship();}
	};
	
	public class Ball extends BallHolder implements Drawable
	{
		public static final int Ball_RADIUS = 10;
		private final Color BALL_COLOR = Color.GREEN;
		private final Ellipse2D ballShape = 
			new Ellipse2D.Float(-Ball_RADIUS,-Ball_RADIUS,2*Ball_RADIUS,2*Ball_RADIUS);
		
		private Player player;
		private Connector connector;
		
		public Ball()
		{
			connector = new Connector();
		}
		
		/**
		 * Sets the connector according to the id: connects this ball to the desired ship.
		 * @return The player that corresponds to the id.
		 */
		private Player setPlayer()
		{	
			if(id==Player.NO_ID)
			{
				player = null;
			}
			else
			{
				player = getPlayer(id);
			}
			
			return player;
		}
		
		private void setConnector()
		{
			if(player!=null)
				connector.setConnector(x, y, (short)player.getShip().getX(), (short)player.getShip().getY(), (byte)0);
		}
		
		@Override
		public void setFrom(Holder<BallHolder> other)
		{
			int previousPlayer = super.id;
			super.setFrom(other);
			
			//if(previousPlayer!=super.id)
			setPlayer();
			setConnector();
		}
		
		public void paintDrawable(Graphics2D g2d)
		{
			//AffineTransform saved = g2d.getTransform();

			g2d.setColor(BALL_COLOR);
			
			int x = Utilities.wrap(map.getWidth(), viewX, this.x);
			int y = Utilities.wrap(map.getHeight(), viewY, this.y);
			
			g2d.translate(x, y);

			g2d.fill(ballShape);
			g2d.translate(-x, -y);
			//g2d.setTransform(saved);

			if(player!=null)
				connector.paintDrawable(g2d);
		}	
	}
	public final Factory<Ball> ballFactory = new Factory<Ball>(){
		public Ball newInstance(){return new Ball();}
	};
	
	
	public class Connector extends ConnectorHolder implements Drawable
	{
		private final Color CONNECTOR_COLOR = Color.GREEN;
		private final Line2D.Float connectorShape= new Line2D.Float();

		/**
		 * Makes sure that the connector is wrapped properly.
		 */
		private void checkWrap()
		{
			//Utilities.wrapLine(map.getWidth(), map.getHeight(), connectorShape);
			
			Utilities.wrapLine(map.getWidth(), map.getHeight(),
								viewX, viewY, connectorShape);
			Utilities.wrapLine(map.getWidth(), map.getHeight(), connectorShape);
			
			//System.out.println("Wrapping connector!");
		}
		
		public void paintDrawable(Graphics2D g2d)
		{	
			//AffineTransform saved = g2d.getTransform();
			
			g2d.setColor(CONNECTOR_COLOR);
			
			connectorShape.setLine(x0, y0, x1, y1);
			checkWrap();
			
			g2d.draw(connectorShape);
			
			//g2d.setTransform(saved);
		}
	}
	public final Factory<Connector> connectorFactory = new Factory<Connector>(){
		public Connector newInstance(){return new Connector();}
	};
	
	public class Mine extends MineHolder implements Drawable
	{	
		public static final String EXPIRED_MINE_NAME = "Expired";
		
		private final Color MINE_COLOR = Color.CYAN;
		public static final int X_RADIUS = 10, Y_RADIUS = 5;
		private final Ellipse2D.Float mineShape= new Ellipse2D.Float(-X_RADIUS, -Y_RADIUS, 2*X_RADIUS, 2*Y_RADIUS);
		private Player player;
		private String name;
		
		public void setFrom(MineHolder other)
		{
			super.setFrom(other);
			
			if (id==EXPIRED_MINE_ID) player = null;
			else player = getPlayer(id);
			
			name = getMineName();
		}
		
		private String getMineName()
		{
			if (id==EXPIRED_MINE_ID) return EXPIRED_MINE_NAME;
			
			if (player==null)
				return null;
			
			return player.getNick();
		}
		
		public void paintDrawable(Graphics2D g2d)
		{
			g2d.setColor(MINE_COLOR);
			int x = Utilities.wrap(map.getWidth(), viewX, this.x);
			int y = Utilities.wrap(map.getHeight(), viewY, this.y);
			
			g2d.translate(x, y);
			g2d.fill(mineShape);
			
			if (name!=null)
			{
				Utilities.drawAdjustedStringDown(g2d, name, 0, -Y_RADIUS);
			}
		}
	}
	public final Factory<Mine> mineFactory = new Factory<Mine>(){
		public Mine newInstance(){return new Mine();}
	};
	
	public class Missile extends MissileHolder implements Drawable
	{
		public static final int MISSILE_WIDTH = 4;
		private final Color MISSILE_COLOR = Color.WHITE;
		private final Rectangle2D.Float missileShape= new Rectangle2D.Float();
		
		public void paintDrawable(Graphics2D g2d)
		{
			g2d.setColor(MISSILE_COLOR);
			
			missileShape.setRect(-len/2, MISSILE_WIDTH/2, len, MISSILE_WIDTH);
			g2d.translate(x, y);
			g2d.rotate(Utilities.getAngleFrom128(dir));
			g2d.fill(missileShape);
		}
	}
	public final Factory<Missile> missileFactory = new Factory<Missile>(){
		public Missile newInstance(){return new Missile();}
	};
	
	private abstract class AbstractDebris<T extends AbstractDebris<T>> extends AbstractDebrisHolder implements Drawable
	{

		//drawing info
		protected Color COLOR;
		protected Ellipse2D debrisShape;

		//methods/variables for dealing with areas
		/**
		 * 256 = 1+max value of an unsigned byte
		 */
		public static final int AREA_SIZE = 256;

		/**
		 * @param type The area type.
		 * @return The x-coordinate for the area: type % 4 - 2
		 */
		private int getXArea(int type)
		{
			return type%4 - 2;
		}

		/**
		 * @param type The area type.
		 * @return The y-coordinate for the area: type / 4 - 6
		 */
		private int getYArea(int type)
		{
			return type / 4 - 6;
		}

		public AbstractDebris(){}
		public AbstractDebris(Color c, Ellipse2D shape)
		{
			COLOR = c;
			debrisShape = shape;
		}

		public void paintDrawable(Graphics2D g2d)
		{
			g2d.setColor(COLOR);
			g2d.translate(x+viewX+getXArea(type)*AREA_SIZE, y+viewY+getYArea(type)*AREA_SIZE);
			g2d.fill(debrisShape);
		}
	}
	
	public class FastShot extends AbstractDebris<FastShot>
	{
		//drawing info
		public static final int SHOT_RADIUS = 2;
		private final Color TEAM_COLOR = Color.BLUE;
		private final Color ENEMY_COLOR = Color.WHITE;
		private final Ellipse2D shotShape = 
			new Ellipse2D.Float(-SHOT_RADIUS,-SHOT_RADIUS,2*SHOT_RADIUS,2*SHOT_RADIUS);
		
		public FastShot()
		{
			super.debrisShape = shotShape;
			//super.COLOR = ENEMY_COLOR;
		}
		
		@Override
		public void setFrom(Holder<AbstractDebrisHolder> other)
		{
			super.setFrom(other);
			setColor();
		}
		
		/**
		 * Sets the shots color to ENEMY_COLOR (white).
		 * TODO: Set the shot's color based on the type.
		 */
		private void setColor()
		{
			super.COLOR = ENEMY_COLOR;
		}
	}
	public final Factory<FastShot> fastShotFactory = new Factory<FastShot>(){
		public FastShot newInstance(){return new FastShot();}
	};
	
	
	public class Spark extends AbstractDebris<Spark>
	{
		//drawing info
		public static final float SPARK_RADIUS = (float)0.5;
		private final Color SPARK_COLOR = Color.RED;
		private final Ellipse2D sparkShape = 
			new Ellipse2D.Float(-SPARK_RADIUS,-SPARK_RADIUS,2*SPARK_RADIUS,2*SPARK_RADIUS);
		
		private void setDebris()
		{
			//super.COLOR = SPARK_COLOR;
			super.debrisShape = sparkShape;
		}
		
		public Spark()
		{
			setDebris();
		}
		
		@Override
		public void setFrom(Holder<AbstractDebrisHolder> other)
		{
			super.setFrom(other);
			setColor();
		}
		
		private void setColor()
		{
			super.COLOR = SPARK_COLOR;
		}
	}
	public final Factory<Spark> sparkFactory = new Factory<Spark>(){
		public Spark newInstance(){return new Spark();}
	};
	
	/**
	 * Class that extends Base to draw player names as well.
	 * @author vlad
	 */
	private class DrawableBase extends Base
	{
		private Player player;
		
		public DrawableBase(Base other)
		{
			super(other.num, other.team, other.base_type, other.x, other.y);
		}
		
		@Override
		public void setFrom(Holder<BaseHolder> other)
		{
			super.setFrom(other);
			
			if(super.id == Player.NO_ID)
			{
				player = null;
			}
			else
			{
				player = getPlayer(id);
			}
		}
		
		@Override
		public void paintDrawable(Graphics2D g2d)
		{
			int x = Utilities.wrap(map.getWidth(), viewX, super.x*BLOCK_SIZE);
			int y = Utilities.wrap(map.getHeight(), viewY, super.y*BLOCK_SIZE);
			
			super.paintDrawable(g2d);
			
			if(player!=null && super.id != Player.NO_ID)
			{
				//System.out.println("Drawing base name");
				String nick = player.getNick();

				switch(base_type)
				{
				case UP: Utilities.drawAdjustedStringDown(g2d, nick, x+BLOCK_SIZE/2, y);
				break;	
				case DOWN: Utilities.drawAdjustedStringUp(g2d, nick, x+BLOCK_SIZE/2, y+BLOCK_SIZE);
				break;	
				case LEFT: Utilities.drawAdjustedStringRight(g2d, nick, x, y+BLOCK_SIZE/2);
				break;	
				case RIGHT: Utilities.drawAdjustedStringLeft(g2d, nick, x+BLOCK_SIZE, y+BLOCK_SIZE/2);
				break;
				}
			}
		}
	}
}