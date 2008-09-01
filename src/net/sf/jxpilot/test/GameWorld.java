package net.sf.jxpilot.test;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Class that manages the various objects in the world.
 * @author vlad
 *
 */
public class GameWorld {
	
	private BlockMap map;
	private MapSetup setup;
	
	private Vector<Collection<? extends Drawable>> drawables;
	
	private Collection<DrawableHandler<? extends ExtendedDrawable<?>>> drawableHandlers;
	
	//various collections to handle 
	/**
	 * Map holding the players by id number.
	 */
	private HashMap<Short, Player> playerMap = new HashMap<Short, Player>();
	private DrawableHandler<FastShot> shotHandler;
	private final int SHOTS_SIZE = 300;
	private DrawableHandler<Connector> connectorHandler;
	private final int CONNECTORS_SIZE = 10;
	private DrawableHandler<Ball> ballHandler;
	private final int BALLS_SIZE = 10;
	private DrawableHandler<Mine> mineHandler;
	private final int MINES_SIZE = 20;
	private DrawableHandler<Spark> sparkHandler;
	private final int DEBRIS_SIZE = 200;
	private DrawableHandler<Missile> missileHandler;
	private final int MISSILE_SIZE = 20;
	
	/**
	 * The current view position.
	 */
	private short viewX, viewY;
	
	public GameWorld(BlockMap map)
	{
		this.map = map;
		this.setup = map.getSetup();
		
		drawables = new Vector<Collection<? extends Drawable>>();
		drawables.add(playerMap.values());
		initDrawableHandlers();
	}
	
	private void initDrawableHandlers()
	{
		drawableHandlers = new ArrayList<DrawableHandler<? extends ExtendedDrawable<?>>>();
		
		shotHandler = new DrawableHandler<FastShot>(new FastShot(), SHOTS_SIZE);
		drawableHandlers.add(shotHandler);
		
		ballHandler = new DrawableHandler<Ball>(new Ball(), BALLS_SIZE);
		drawableHandlers.add(ballHandler);
		
		connectorHandler = new DrawableHandler<Connector>(new Connector(), CONNECTORS_SIZE);
		drawableHandlers.add(connectorHandler);
		
		mineHandler = new DrawableHandler<Mine>(new Mine(), MINES_SIZE);
		drawableHandlers.add(mineHandler);
		
		sparkHandler = new DrawableHandler<Spark>(new Spark(), DEBRIS_SIZE);
		drawableHandlers.add(sparkHandler);
		
		missileHandler = new DrawableHandler<Missile>(new Missile(), MISSILE_SIZE);
		drawableHandlers.add(missileHandler);
		
		drawables.addAll(drawableHandlers);
	}
	
	public BlockMap getMap(){return map;}
	
	public short getSelfX(){return viewX;}
	public short getSelfY(){return viewY;}
	public void setViewPosition(short x, short y)
	{
		viewX = x;
		viewY = y;
	}
	
	public DrawableHandler<Ball> getBallHandler(){return ballHandler;}
	public DrawableHandler<Connector> getConnectorHandler(){return connectorHandler;}
	public DrawableHandler<Mine> getMineHandler(){return mineHandler;}
	public DrawableHandler<Missile> getMissileHandler(){return missileHandler;}
	public DrawableHandler<FastShot> getShotHandler(){return shotHandler;}
	public DrawableHandler<Spark> getSparkHandler(){return sparkHandler;}
	
	public Vector<Collection<? extends Drawable>> getDrawables(){return drawables;}
	
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
	
	public void addBall(BallHolder ball)
	{
		ballHandler.addDrawable(ball);
	}
	
	public void addConnector(ConnectorHolder connector)
	{
		connectorHandler.addDrawable(connector);
	}
	
	public void addMine(MineHolder mine)
	{
		mineHandler.addDrawable(mine);
	}
	
	public void addMissile(MissileHolder missile)
	{
		missileHandler.addDrawable(missile);
	}
	
	public void addFastShot(AbstractDebrisHolder shot)
	{
		shotHandler.addDrawable(shot);
		//System.out.println("Setting FastShot in GameWorld");	
	}
	
	public void addSpark(AbstractDebrisHolder spark)
	{
		sparkHandler.addDrawable(spark);
		//shotHandler.addDrawable(spark);		
	}
	
	/**
	 * Clears all in-game objects so that they are refreshed each frame.
	 */
	public void clearDrawables()
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
	
	//inner Drawable classes	
	public class Ball extends BallHolder implements ExtendedDrawable<Ball>
	{
		public static final int Ball_RADIUS = 10;
		private final Color BALL_COLOR = Color.GREEN;
		private final Ellipse2D ballShape = 
			new Ellipse2D.Float(-Ball_RADIUS,-Ball_RADIUS,2*Ball_RADIUS,2*Ball_RADIUS);
		
		private Connector connector;
		
		public Ball()
		{
			connector = new Connector();
		}
		
		public Ball getNewInstance()
		{
			return new Ball();
		}
		
		public void set(Ball other)
		{
			super.set(other);
		}
		
		/**
		 * Sets the connector according to the id: connects this ball to the desired ship.
		 * @return True if the id is valid.
		 */
		private boolean setConnector()
		{
			Player p = getPlayer(id);
			if (p==null) return false;
			
			connector.setConnector(x, y, (short)p.getX(), (short)p.getY(), (byte)0);
			
			return true;
		}
		
		
		public void paintDrawable(Graphics2D g2d)
		{
			AffineTransform saved = g2d.getTransform();

			g2d.setColor(BALL_COLOR);
			g2d.translate(x, y);

			g2d.fill(ballShape);

			g2d.setTransform(saved);

			if (id>=0)
			{
				if (setConnector());
				connector.paintDrawable(g2d);
			}
		}
		
	}
	
	public class Connector extends ConnectorHolder implements ExtendedDrawable<Connector>
	{
		private final Color CONNECTOR_COLOR = Color.GREEN;
		private final Line2D.Float connectorShape= new Line2D.Float();

		public Connector getNewInstance()
		{return new Connector();}
		
		@Override
		public void set(Connector other)
		{
			super.set(other);			
		}
		
		/**
		 * Makes sure that the connector is wrapped properly.
		 */
		private void checkWrap()
		{
			Utilities.wrapLine(map.getWidth(), map.getHeight(), connectorShape);
			//System.out.println("Wrapping connector!");
		}
		
		public void paintDrawable(Graphics2D g2d)
		{	
			AffineTransform saved = g2d.getTransform();
			
			g2d.setColor(CONNECTOR_COLOR);
			
			connectorShape.setLine(x0, y0, x1, y1);
			checkWrap();
			
			g2d.draw(connectorShape);
			
			g2d.setTransform(saved);
		}
	}
	
	public class Mine extends MineHolder implements ExtendedDrawable<Mine>
	{
		private final Color MINE_COLOR = Color.CYAN;
		public static final int X_RADIUS = 10, Y_RADIUS = 5;
		private final Ellipse2D.Float mineShape= new Ellipse2D.Float(-X_RADIUS, -Y_RADIUS, 2*X_RADIUS, 2*Y_RADIUS);
		
		public Mine getNewInstance()
		{return new Mine();}
		
		public void set(Mine other)
		{
			super.set(other);
		}
		
		public void paintDrawable(Graphics2D g2d)
		{
			AffineTransform saved = g2d.getTransform();
			
			g2d.setColor(MINE_COLOR);
			g2d.translate(x, y);
			g2d.fill(mineShape);
			
			g2d.setTransform(saved);
		}
	}
	
	public class Missile extends MissileHolder implements ExtendedDrawable<Missile>
	{
		public static final int MISSILE_WIDTH = 4;
		private final Color MISSILE_COLOR = Color.WHITE;
		private final Rectangle2D.Float missileShape= new Rectangle2D.Float();
		
		public Missile getNewInstance()
		{return new Missile();}
		
		public void set(Missile other)
		{
			super.set(other);
		}
		
		public void paintDrawable(Graphics2D g2d)
		{
			AffineTransform saved = g2d.getTransform();
			
			g2d.setColor(MISSILE_COLOR);
			
			missileShape.setRect(-len/2, MISSILE_WIDTH/2, len, MISSILE_WIDTH);
			g2d.translate(x, y);
			g2d.rotate(Utilities.getAngleFrom128(dir));
			g2d.fill(missileShape);
			
			g2d.setTransform(saved);
		}
	}
	
	private abstract class AbstractDebris<T extends AbstractDebris<T>> extends AbstractDebrisHolder implements ExtendedDrawable<T>
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

		
		@Override
		public void set(T other)
		{
			setAbstractDebris(other.type, other.x, other.y);
			System.out.println("Setting AbstractDebris in GameWorld");
		}
		
		/*
		@Override
		public void set(AbstractDebrisHolder other)
		{
			this.set(other);
			System.out.println("Setting FastShot in GameWorld");
		}
		*/
		
		public int getType(){return type;}
		public short getX(){return x;}
		public short getY(){return y;}
		
		public void paintDrawable(Graphics2D g2d)
		{
			AffineTransform saved = g2d.getTransform();
			
			g2d.setColor(COLOR);
			g2d.translate(x+viewX+getXArea(type)*AREA_SIZE, y+viewY+getYArea(type)*AREA_SIZE);
			g2d.fill(debrisShape);
			
			g2d.setTransform(saved);
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
			super.COLOR = ENEMY_COLOR;
		}
		
		public FastShot getNewInstance()
		{
			return new FastShot();
		}
		
		@Override
		public void set(FastShot other)
		{
			super.set(other);
			setColor();
			System.out.println("Setting FastShot in GameWorld");	
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
	
	public class Spark extends AbstractDebris<Spark>
	{
		//drawing info
		public static final float SPARK_RADIUS = (float)0.5;
		private final Color SPARK_COLOR = Color.RED;
		private final Ellipse2D sparkShape = 
			new Ellipse2D.Float(-SPARK_RADIUS,-SPARK_RADIUS,2*SPARK_RADIUS,2*SPARK_RADIUS);
		
		private void setDebris()
		{
			super.COLOR = SPARK_COLOR;
			super.debrisShape = sparkShape;
		}
		
		private Spark()
		{
			setDebris();
		}

		public Spark getNewInstance()
		{
			return new Spark();
		}
		
		public void set(Spark other)
		{
			super.set(other);
		}
	}
}