package net.sf.jxpilot.test;

import java.util.*;
import static net.sf.jxpilot.test.MapBlock.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import static net.sf.jxpilot.MathFunctions.*;

public class JXPilotFrame extends JFrame
{
	/**
	 * Whether or not to try to recenter the mouse after every movement.
	 */
	public static final boolean MOUSE_RECENTERING = false;
	
    /**
	 * Whether the MapFrame attempts to use Full Screen Exclusive Mode.
	 * Otherwise uses USF (undecorated full screen)
	 */
	private boolean FSEM = true;

	/**
	 * Whether or not the mouse should be used to control user input.
	 */
	private boolean mouseControl = false;
	
	private AffineTransform identity = new AffineTransform();
	private Color blockColor = Color.BLUE;
	private Color spaceColor = Color.BLACK;
	private Color shipColor = Color.white;

	private Toolkit toolkit = Toolkit.getDefaultToolkit();
	private Dimension screenSize = toolkit.getScreenSize();
	
	private ClientInputListener clientInputListener;
	
	private BlockMap blockMap;
	private MapSetup setup;
	private MapBlock[][] blocks;
	
	/**
	 * center of viewing screen, in blocks
	 */
	private double viewX, viewY;
	
	public static final int defaultViewSize = 27;
	
	/**
	 * number of blocks in the screen
	 */
	private int viewSize;
	
	/**
	 * current transform of screen
	 */
	private AffineTransform currentTransform = new AffineTransform();
	/**
	 * transform for switching between Cartesian mode and AWT mode. (y is changed with -y)
	 */
	private AffineTransform flippedTransform = new AffineTransform();
	
	
	private Vector<Collection<? extends Drawable>> drawables;

	/**
	 * Messages to draw.
	 */
	private MessagePool messagePool = null;
	
	/**
	 * Maps local keyboard to abstract XPilot keyboard which is sent to server.
	 * Note that each actual key may represent multiple abstract XPilot keys.
	 */
	private HashMap<Integer, Byte[]> keyPreferences;
	
	/**
	 * Maps mouse buttons to abstract XPilot keyboard which is sent to server.
	 */
	private HashMap<Integer, Byte[]> mousePreferences;
	
	/**
	 * Maps keyboard to various user options.
	 */
	private HashMap<Integer, UserOption> userPreferences;
	
	/**
	 * Maps user options to the actual OptionHandlers.
	 */
	private EnumMap<UserOption, OptionHandler> optionHandlers;
	
	
	public JXPilotFrame(BlockMap blockMap, ClientInputListener l)
	{	
		clientInputListener = l;
		
		defaultKeyInit();
		defaultMouseInit();
		defaultUserInit();
		optionsInit();
		//this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		//this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		//this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		
		this.blockMap = blockMap;
		setup = blockMap.getSetup();
		blocks = blockMap.getBlocks();
		
		viewX=setup.getX()/2.0;
		viewY=setup.getY()/2.0;
		
		viewSize = defaultViewSize;
		
		//buffer = new BufferedImage(this.getWidth(),this.getHeight(),BufferedImage.TYPE_INT_RGB);
		
		//g2d = buffer.createGraphics();
		
		//currentTransform.translate(-viewX, -viewY);
		
		flippedTransform.scale(1, -1);
		flippedTransform.translate(0, -setup.getY()*BLOCK_SIZE);
		
		setTransform();
		//setTransform();

		FSEM = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().isFullScreenSupported();

		messagePool = new MessagePool();

		if (FSEM)
			initFullScreen();
		else
		{
			this.setUndecorated(true);
			this.setIgnoreRepaint(true);
			this.setSize(screenSize);
			this.setVisible(true);
			setBufferStrategy();
		}
		
		initBuffers();
		
		//pack();
		
		this.addKeyListener(new KeyAdapter()
		{
			
			public void keyPressed(KeyEvent e)
			{
				/*
				switch (e.getKeyCode())
				{
				
				case KeyEvent.VK_RIGHT:
					moveView(1, 0);
					break;
				case KeyEvent.VK_LEFT:
					moveView(-1, 0);
					break;
				case KeyEvent.VK_UP:
					moveView(0,1);
					break;
				case KeyEvent.VK_DOWN:
					moveView(0,-1);
					break;
				case KeyEvent.VK_COMMA:
					viewSize += 1;
					break;
				case KeyEvent.VK_PERIOD:
					viewSize -= 1;
					break;
				case KeyEvent.VK_ESCAPE:
					clientInputListener.quit();
					break;
				}
				*/
				
				int key = e.getKeyCode();
				
				if (userPreferences.containsKey(key))
				{
					optionHandlers.get(userPreferences.get(key)).fireOption();
					
					//prevents user options from toggling XPilot commands
					return;
				}
				
				if (keyPreferences.containsKey(key))
				{
					for (byte b : keyPreferences.get(key))
					clientInputListener.setKey(b, true);
				}
			}

			public void keyReleased(KeyEvent e)
			{
				int key = e.getKeyCode();
				
				if (keyPreferences.containsKey(key))
				{
					for (byte b : keyPreferences.get(key))
					clientInputListener.setKey(b, false);
				}
			}		
		});

		this.addMouseListener(new MouseAdapter()
		{
			
			public void mousePressed(MouseEvent e)
			{
				if (!mouseControl) return;
				
				int button = e.getButton();
				if (mousePreferences.containsKey(button))
				{
					for(byte b : mousePreferences.get(button))
					{
						clientInputListener.setKey(b, true);
					}
				}
			}
			
			public void mouseReleased(MouseEvent e)
			{
				if (!mouseControl) return;
				
				int button = e.getButton();
				if (mousePreferences.containsKey(button))
				{
					for(byte b : mousePreferences.get(button))
					{
						clientInputListener.setKey(b, false);
					}
				}			
			}
		});
		
		this.addMouseMotionListener(new MouseMotionHandler());
	}
	
	/**
	 * Class that handles MouseMovement. It sends movement to the server so the ship can turn.
	 * It also attempts to move the mouse pointer back to the center of the screen, if allowed by the system.
	 * @author vlad
	 */
	private class MouseMotionHandler extends MouseMotionAdapter
	{

		private Robot robot;
		private int mouseX;
		private boolean robotMovement;
		private int centerX = screenSize.width/2,
					centerY = screenSize.height/2;
		
		public MouseMotionHandler()
		{
			if (MOUSE_RECENTERING)
			try
			{
				robot = new Robot();
				this.movePointerBack();
				//mouseX = screenSize.width/2;
			}
			catch(AWTException e)
			{
				System.out.println("Can't control mouse movements :(");
				robot = null;
			}
		}
		
		public void mouseMoved(MouseEvent e)
		{
			if (!mouseControl) return;
			
			
			//if (!robotMovement) 
				handleMove(e);
		}
		
		public void mouseDragged(MouseEvent e)
		{
			if (!mouseControl) return;
			
			//if (!robotMovement) 
				handleMove(e);
		}
		
		
		private void handleMove(MouseEvent e)
		{
			if (MOUSE_RECENTERING)
			// this event is from re-centering the mouse - ignore it
		    if (robotMovement && centerX == e.getX()
		        && centerY == e.getY()) {
		    	robotMovement = false;
		    }
		    else
		    {
				clientInputListener.movePointer((short)((e.getX()-mouseX)));
				//mouseX = e.getX();
				if(robot!=null)
					movePointerBack();
				else
				{
					mouseX = e.getX();				
				}
		    }
			else
			{
				clientInputListener.movePointer((short)((e.getX()-mouseX)));
				mouseX = e.getX();
			}
		}
		
		private void movePointerBack()
		{
			robotMovement = true;
			robot.mouseMove(screenSize.width/2,screenSize.height/2);
			mouseX = screenSize.width/2;
			//robotMovement = false;
		}
	}
	
	private Image blankImage = toolkit.createImage(new byte[]{0});
	private Cursor noCursor = toolkit.createCustomCursor(blankImage, new Point(1,1), "No Cursor");
	
	private void hideCursor()
	{
		setCursor(noCursor);
	}
	
	private void showCursor()
	{
		this.setCursor(Cursor.getDefaultCursor());
	}
	
	/**
	 * sets default mouse actions
	 */
	
	private void defaultMouseInit()
	{
		mousePreferences = new HashMap<Integer, Byte[]>();
		
		mousePreferences.put(MouseEvent.BUTTON1, new Byte[]{Keys.KEY_FIRE_SHOT});
		mousePreferences.put(MouseEvent.BUTTON3, new Byte[]{Keys.KEY_THRUST});
	}
	
	//keyboard stuff
	/**
	 * sets default keys 
	 */
	private void defaultKeyInit()
	{
		keyPreferences = new HashMap<Integer, Byte[]>();
		
		keyPreferences.put(KeyEvent.VK_ENTER,	new Byte[] {Keys.KEY_FIRE_SHOT});
		keyPreferences.put(KeyEvent.VK_A, 		new Byte[] {Keys.KEY_TURN_LEFT});
		keyPreferences.put(KeyEvent.VK_S, 		new Byte[] {Keys.KEY_TURN_RIGHT});
		keyPreferences.put(KeyEvent.VK_SHIFT, 	new Byte[] {Keys.KEY_THRUST});
		keyPreferences.put(KeyEvent.VK_CONTROL, new Byte[] {Keys.KEY_CONNECTOR});
		keyPreferences.put(KeyEvent.VK_H, 		new Byte[] {Keys.KEY_CHANGE_HOME});
		keyPreferences.put(KeyEvent.VK_D, 		new Byte[] {Keys.KEY_DROP_BALL});
		keyPreferences.put(KeyEvent.VK_PAUSE, 	new Byte[] {Keys.KEY_PAUSE});
		keyPreferences.put(KeyEvent.VK_M,		new Byte[] {Keys.KEY_TALK} );
		//keyPreferences.put(KeyEvent.VK_F5, Keys.)
	}
	
	private void optionsInit()
	{
		optionHandlers = new EnumMap<UserOption, OptionHandler>(UserOption.class);
		
		optionHandlers.put(UserOption.QUIT, new OptionHandler()
		{
			public void fireOption()
			{
				clientInputListener.quit();
			}
		});
		
		optionHandlers.put(UserOption.TOGGLE_MOUSE_CONTROL, new OptionHandler()
		{
			public void fireOption()
			{
				if (mouseControl)
				{
					showCursor();
					mouseControl = false;
				}
				else
				{
					hideCursor();
					mouseControl = true;	
				}
			}
		});
	}
	
	private interface OptionHandler{
		public void fireOption();
	}

	private void defaultUserInit()
	{
		userPreferences = new HashMap<Integer, UserOption>();
		
		userPreferences.put(KeyEvent.VK_ESCAPE, UserOption.QUIT);
		userPreferences.put(KeyEvent.VK_K, UserOption.TOGGLE_MOUSE_CONTROL);	
	}
	
	//graphics stuff
	private final int NUM_BUFFERS = 2;
	private GraphicsDevice gd;
	private Graphics2D screenG2D;
	private BufferStrategy bufferStrategy;
	
	private BufferedImage mapBuffer;
	private BufferedImage worldBuffer;
	private Graphics2D worldG2D;
	
	private void initFullScreen()
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getDefaultScreenDevice();
		
		this.setUndecorated(true);
		this.setIgnoreRepaint(true);
		this.setResizable(false);
		
		if (!gd.isFullScreenSupported())
		{
			System.out.println("FSEM not supported.");
			System.exit(0);
		}
		gd.setFullScreenWindow(this);
		
		setBufferStrategy();
	}
	
	private void setBufferStrategy()
	{
		try
		{
			EventQueue.invokeAndWait(new Runnable(){
				public void run()
				{
					JXPilotFrame.this.createBufferStrategy(NUM_BUFFERS);
				}
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Error while creating buffer strategy.");
			System.exit(0);
		}
		
		try
		{
			Thread.sleep(500);
		}
		catch(InterruptedException e){}
		
		bufferStrategy = this.getBufferStrategy();
	}
	
	private void restoreScreen()
	{
		Window w = gd.getFullScreenWindow();
		if(w!=null)
		{
			w.dispose();
		}
		gd.setFullScreenWindow(null);
	}
	
	/**
	 * Restores screen and disposes of this frame.
	 */
	public void finish()
	{
		if (FSEM)
			restoreScreen();
		
		//clientInputListener.quit();
		this.dispose();
	}
	
	private void initBuffers()
	{
		mapBuffer = createMapBuffer();
		
		if (FSEM)
			worldBuffer = gd.getDefaultConfiguration().createCompatibleImage(mapBuffer.getWidth(), mapBuffer.getHeight());
		else
			worldBuffer = new BufferedImage(mapBuffer.getWidth(), mapBuffer.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		worldG2D = worldBuffer.createGraphics();
		worldG2D.setTransform(flippedTransform);
	}

	public void setDrawables(Vector<Collection<? extends Drawable>> d)
	{
		drawables = d;
	}

	private void setTransform()
	{
		//currentTransform.setToIdentity();
		double scale = (double)this.getHeight()/(viewSize*BLOCK_SIZE);
		//System.out.println("Scale = " + scale + "\nHeight = " + this.getHeight());
		
		currentTransform.setToIdentity();
		currentTransform.translate(this.getWidth()/2.0, this.getHeight()/2.0);
		currentTransform.scale(scale, scale);
		currentTransform.translate(-viewX*BLOCK_SIZE, viewY*BLOCK_SIZE-setup.getY()*BLOCK_SIZE);
	}
	
	/*
	public void renderGame()
	{
		panel.renderGame();
	}
	*/
	
	public void activeRender()
	{
			try
			{
				screenG2D = (Graphics2D)bufferStrategy.getDrawGraphics();
				renderGame(screenG2D);
				screenG2D.dispose();
				if (!bufferStrategy.contentsLost())
				{
					bufferStrategy.show();
				}
				else
				{
					System.out.println("BufferStrategy contents lost");
				}
				Toolkit.getDefaultToolkit().sync();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		
		//panel.paintScreen();
	}
	
	/**
	 * Sets the view focus in blocks, measured in regular Cartesian form.
	 */
	public void setView(double x, double y)
	{
		viewX = trueMod(x, setup.getX());
		viewY = trueMod(y, setup.getY());
	}
	
	public void moveView(double dx, double dy)
	{
		setView(viewX+dx, viewY+dy);
	}
	
	//private BufferedImage screenBuffer;
	//private Graphics2D screenG2D;
	
	
	private AffineTransform translated = new AffineTransform();

	private void renderGame(Graphics2D screenG2D)
	{
		//paintWorld();
		setTransform();
		//paintWorldGraphics();
		
		for(int x= -1; x<=1;x++)
		{
			for (int y = -1; y<=1;y++)
			{
				translated.setTransform(currentTransform);
				translated.translate(x*setup.getX()*BLOCK_SIZE, y*setup.getY()*BLOCK_SIZE);
				//screenG2D.setTransform(currentTransform);
				//screenG2D.translate(x*setup.getX()*BLOCK_SIZE, y*setup.getY()*BLOCK_SIZE);
				paintWorld(screenG2D, translated);
			}
		}

		screenG2D.setTransform(identity);
//		screenG2D.setColor(Color.WHITE);
//		screenG2D.drawString("TEST", 30, 30);

		messagePool.render(screenG2D);

		//screenG2D.setTransform(currentTransform);
		//screenG2D.drawImage(mapBuffer, 0, 0, this);
	}

	private BufferedImage createMapBuffer()
	{
		BufferedImage temp;
		
		if (FSEM)
			temp = gd.getDefaultConfiguration().createCompatibleImage(setup.getX()*BLOCK_SIZE, setup.getY()*BLOCK_SIZE);
		else
			temp = new BufferedImage(setup.getX()*BLOCK_SIZE, setup.getY()*BLOCK_SIZE, BufferedImage.TYPE_INT_RGB);
		
		Graphics2D g2d = temp.createGraphics();

		g2d.setColor(spaceColor);
		g2d.fillRect(0, 0, temp.getWidth(), temp.getHeight());

		g2d.setTransform(flippedTransform);

		paintBlocks(g2d);

		return temp;
	}

	private void paintWorldGraphics()
	{
		//worldG2D.setTransform(identity);
		worldG2D.drawImage(mapBuffer, 0, 0, this);

		worldG2D.setTransform(flippedTransform);
		
		
		if (drawables!=null)
		{
			for (Collection<? extends Drawable> c : drawables)
			{
				if (c!=null)
					for (Drawable d : c)
					{
						worldG2D.setTransform(flippedTransform);
						//System.out.println("\nPainting drawable: ****************************************");
						d.paintDrawable(worldG2D);
					}
			}
		}
		
	}
	
	private void paintWorld(Graphics2D g2d)
	{
		g2d.drawImage(mapBuffer, 0, 0, this);
	}
	
	private void paintWorld(Graphics2D g2d, AffineTransform transform)
	{
		g2d.setTransform(transform);
		g2d.drawImage(mapBuffer, 0, 0, this);
		
		g2d.transform(flippedTransform);
		
		if (drawables!=null)
		{
			for (Collection<? extends Drawable> c : drawables)
			{
				if (c!=null)
					for (Drawable d : c)
					{
						//g2d.setTransform(transform);
						
						//g2d.setTransform(transform);
						//g2d.transform(flippedTransform);
						
						//System.out.println("\nPainting drawable: ****************************************");
						d.paintDrawable(g2d);
					}
			}
		}
	}
	
	private void paintBlocks(Graphics2D g2)
	{
		for (MapBlock[] array : blocks)
		{
			for (MapBlock o : array)
			{		
				paintBlock(g2, o);
			}
		}
	}

	private void paintBlock(Graphics2D g2, MapBlock block)
	{
		if (block.getShape()==null) return;

		//g2.setTransform(identity);
		//g2.translate(block.getX()*block.BLOCK_SIZE, (setup.getY()-block.getY()-1)*block.BLOCK_SIZE);

		g2.setColor(block.getColor());

		g2.translate(block.getX()*BLOCK_SIZE, block.getY()*BLOCK_SIZE);

		if (block.isFilled())
			g2.fill(block.getShape());
		else
			g2.draw(block.getShape());

		g2.translate(-block.getX()*BLOCK_SIZE, -block.getY()*BLOCK_SIZE);
	}

	/**
     * Shows if Full Screen Exclusive Mode is used by this
     * <code>JXPilotFrame</code>.
     * 
     * @return <code>True</code>, if supported.
     */
    public boolean isFSEMSupported() {
        return FSEM;
    }

    /**
     * @see #messagePool
     */
    public MessagePool getMessagePool() {
        return messagePool;
    }

}
