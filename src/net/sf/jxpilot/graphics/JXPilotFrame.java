package net.sf.jxpilot.graphics;

import java.util.*;
import java.awt.event.*;
import net.sf.jxpilot.data.Keys;
import net.sf.jxpilot.map.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import static net.sf.jxpilot.map.MapBlock.*;
import static net.sf.jxpilot.util.Utilities.*;
import net.sf.jxpilot.user.*;
import net.sf.jxpilot.*;
import net.sf.jxpilot.game.*;
import net.sf.jxpilot.util.*;

public class JXPilotFrame extends Frame
{
	/**
	 * Whether or not to try to recenter the mouse after every movement.
	 */
	public static final boolean MOUSE_RECENTERING = true;
	
    /**
	 * Whether the MapFrame attempts to use Full Screen Exclusive Mode.
	 * Otherwise uses AFS (almost full screen)
	 */
	private boolean FSEM = false;
	
	/**
	 * The display mode to be used if no display mode is given.
	 */
	public static final DisplayMode defaultDisplayMode = DisplayMode.UFS;
	
	/**
	 * DisplayMode used by this JXPilotFrame.
	 */
	private DisplayMode displayMode;
	
	/**
	 * Whether or not the mouse should be used to control user input.
	 */
	private boolean mouseControl = false;
	
	private AffineTransform identity = new AffineTransform();

	private Toolkit toolkit = Toolkit.getDefaultToolkit();
	private Dimension screenSize = toolkit.getScreenSize();
	
	private ClientInputListener clientInputListener;
	
	private GameWorld world;
	
	public static final int defaultViewSize = 27;
	
	/**
	 * number of blocks in the screen
	 */
	private int viewSize;

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
	
	private WorldRenderer renderer;
	public JXPilotFrame(DisplayMode mode, GameWorld world, ClientInputListener l)
	{			
		super(Accelerator.gfxConfig);
		
		if(mode==null)
		{
			this.displayMode = defaultDisplayMode;
		}
		else
		{
			this.displayMode = mode;
		}
		
		this.world = world;
		clientInputListener = l;
		
		defaultKeyInit();
		defaultMouseInit();
		defaultUserInit();
		optionsInit();
		
		viewSize = defaultViewSize;
		
		this.world = world;
		renderer = new WorldRenderer(world);		

		FSEM = (displayMode==DisplayMode.FSEM) && GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().isFullScreenSupported();


		if (FSEM)
			initFullScreen();
		else
		{
			
			if(displayMode == DisplayMode.FSEM)
			{
				displayMode = defaultDisplayMode;
			}
			
			if(displayMode == DisplayMode.UFS)
			{
				this.setUndecorated(true);
			}
			
			this.setIgnoreRepaint(true);
			this.setSize(screenSize);
			this.setVisible(true);
			this.setResizable(true);
			setBufferStrategy();
		}
		
		
		
		
		messagePool = new MessagePool();
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
		keyPreferences.put(KeyEvent.VK_CONTROL, new Byte[] {Keys.KEY_CONNECTOR, Keys.KEY_REFUEL});
		keyPreferences.put(KeyEvent.VK_H, 		new Byte[] {Keys.KEY_CHANGE_HOME});
		keyPreferences.put(KeyEvent.VK_D, 		new Byte[] {Keys.KEY_DROP_BALL});
		keyPreferences.put(KeyEvent.VK_PAUSE, 	new Byte[] {Keys.KEY_PAUSE});
		keyPreferences.put(KeyEvent.VK_M,		new Byte[] {Keys.KEY_TALK} );
		keyPreferences.put(KeyEvent.VK_T,		new Byte[] {Keys.KEY_TALK} );
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
		
		optionHandlers.put(UserOption.BALL_GONE, new OptionHandler()
		{
			public void fireOption()
			{
				clientInputListener.talk("***BALL IS GONE SAVE IT NOW***");
			}
		});
		
		optionHandlers.put(UserOption.BALL_SAFE, new OptionHandler()
		{
			public void fireOption()
			{
				clientInputListener.talk("***BALL IS SAFE, DON'T SHOOT YOURSELF***");
			}
		});
		optionHandlers.put(UserOption.COVER, new OptionHandler()
		{
			public void fireOption()
			{
				clientInputListener.talk("***BALL IS APPROACHING BASE COVER NOW***");
			}
		});
		optionHandlers.put(UserOption.BALL_POP, new OptionHandler()
		{
			public void fireOption()
			{
				clientInputListener.talk("***BALL HAS POPPED***");
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
		userPreferences.put(KeyEvent.VK_F1, UserOption.BALL_GONE);
		userPreferences.put(KeyEvent.VK_F2, UserOption.BALL_SAFE);
		userPreferences.put(KeyEvent.VK_F3, UserOption.COVER);
		userPreferences.put(KeyEvent.VK_F4, UserOption.BALL_POP);
	}
	
	//graphics stuff
	private final int NUM_BUFFERS = 2;
	private GraphicsDevice gd;
	private BufferStrategy bufferStrategy;
	
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

	public void activeRender()
	{
			try
			{
				Graphics2D screenG2D = (Graphics2D)bufferStrategy.getDrawGraphics();
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
				//for linux
				Toolkit.getDefaultToolkit().sync();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}
	
	private void renderGame(Graphics2D screenG2D)
	{
		
		renderer.renderGame(screenG2D, super.getWidth()/2.0, super.getHeight()/2.0,
				(viewSize*super.getWidth())/super.getHeight(), viewSize, super.getHeight()/viewSize);
	
		screenG2D.setTransform(identity);
		messagePool.render(screenG2D);

		//screenG2D.setTransform(currentTransform);
		//screenG2D.drawImage(mapBuffer, 0, 0, this);
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