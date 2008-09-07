package net.sf.jxpilot.test.graphics;

import net.sf.jxpilot.test.*;
import java.util.*;
import static net.sf.jxpilot.test.MapBlock.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import static net.sf.jxpilot.MathFunctions.*;

public class TestMapFrame extends JFrame
{
	/**
	 * Whether the MapFrame uses Full Screen Exclusive Mode.
	 */
	public static final boolean FSEM = true;
	
	private AffineTransform identity = new AffineTransform();
	private Color blockColor = Color.BLUE;
	private Color spaceColor = Color.BLACK;
	private Color shipColor = Color.white;

	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
	//private ClientInputListener clientInputListener;
	
	private BlockMap blockMap;
	private BlockMapSetup setup;
	private MapBlock[][] blocks;
	
	//center of viewing screen, in blocks
	private double viewX, viewY;
	//number of blocks in the screen
	public static final int defaultViewSize = 27;
	private int viewSize;
	private AffineTransform currentTransform = new AffineTransform();
	private AffineTransform flippedTransform = new AffineTransform();
	
	private Vector<Collection<? extends Drawable>> drawables;
	
	
	
	/*
	private static final int NUM_KEYS = KeyEvent.KEY_LAST-KeyEvent.KEY_FIRST+1; 
	
	private static int getKeyIndex(int key)
	{
		return key-KeyEvent.KEY_FIRST;
	}
	
	private byte[] keyPreferences = new byte[NUM_KEYS];
	*/
	
	private HashMap<Integer, Byte> keyPreferences;
	
	public TestMapFrame(BlockMap blockMap)
	{	
		defaultKeyInit();
		
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
		
		if (FSEM)
			initFullScreen();
		else
		{
			this.setUndecorated(true);
			this.setIgnoreRepaint(true);
			this.setSize(screenSize);
		}
		
		initBuffers();
		
		//pack();
		
		this.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				
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
					finish();
					break;
				}
				
				//repaint();
				activeRender();
				
				int key = e.getKeyCode();
				
				if (keyPreferences.containsKey(key))
				{
					//clientInputListener.setKey(keyPreferences.get(key), true);
				}
			}

			public void keyReleased(KeyEvent e)
			{
				int key = e.getKeyCode();
				
				if (keyPreferences.containsKey(key))
				{
					//clientInputListener.setKey(keyPreferences.get(key), false);
				}
			}
			
		});
		
		this.addWindowListener(new WindowAdapter()
		{
			
			public void windowActivated(WindowEvent e){}
			public void windowOpened(WindowEvent e){}
				
			public void windowClosed(WindowEvent e)
			{
				//close = true;
				//clientInputListener.quit();
			}

			public void windowClosing(WindowEvent e)
			{
				//clientInputListener.quit();
				//close = true;
			}

		});
	}
	
	private static final int NUM_BUFFERS = 2;
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
					TestMapFrame.this.createBufferStrategy(NUM_BUFFERS);
				}
			});
		}
		catch (Exception e)
		{
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
	
	public void finish()
	{
		if (FSEM)
			restoreScreen();
		
		System.exit(0);
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
	
	/**
	 * sets default keys 
	 */
	private void defaultKeyInit()
	{
		keyPreferences = new HashMap<Integer, Byte>();
		
		keyPreferences.put(KeyEvent.VK_ENTER, Keys.KEY_FIRE_SHOT);
		keyPreferences.put(KeyEvent.VK_A, Keys.KEY_TURN_LEFT);
		keyPreferences.put(KeyEvent.VK_S, Keys.KEY_TURN_RIGHT);
		keyPreferences.put(KeyEvent.VK_SHIFT, Keys.KEY_THRUST);
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
		if (FSEM)
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
		}
		else
		{
			screenG2D = (Graphics2D) this.getGraphics();
			renderGame(screenG2D);
			screenG2D.dispose();
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
	
	private void renderGame(Graphics2D screenG2D)
	{
		//paintWorld();
		setTransform();
		
		for(int x= -1; x<=1;x++)
		{
			for (int y = -1; y<=1;y++)
			{
				screenG2D.setTransform(currentTransform);
				screenG2D.translate(x*setup.getX()*BLOCK_SIZE, y*setup.getY()*BLOCK_SIZE);
				screenG2D.drawImage(mapBuffer, 0, 0, this);
			}
		}
		

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

		//worldG2D.setTransform(flippedTransform);

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
		
		if (drawables!=null)
		{
			for (Collection<? extends Drawable> c : drawables)
			{
				if (c!=null)
					for (Drawable d : c)
					{
						//g2d.setTransform(flippedTransform);
						//System.out.println("\nPainting drawable: ****************************************");
						d.paintDrawable(worldG2D);
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

}
