package net.sf.jxpilot.test;

import static net.sf.jxpilot.test.MapBlock.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import static net.sf.jxpilot.MathFunctions.*;

public class MapFrame extends JFrame
{
	private AffineTransform identity = new AffineTransform();
	private Color blockColor = Color.BLUE;
	private Color spaceColor = Color.BLACK;
	private BufferedImage buffer;
	private Graphics2D g2d;
	
	
	private Map map;
	private MapSetup setup;
	private MapBlock[][] blocks;
	
	private MapPanel panel;
	
	//center of viewing screen, in blocks
	private double viewX, viewY;
	//number of blocks in the screen
	private int viewSize;
	private AffineTransform currentTransform = new AffineTransform();
	
	
	public MapFrame(Map map)
	{
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		this.map = map;
		setup = map.getSetup();
		blocks = map.getBlocks();
		
		viewX=setup.getX()/2.0;
		viewY=setup.getY()/2.0;
		
		viewSize = 27;
		
		buffer = new BufferedImage(this.getWidth(),this.getHeight(),BufferedImage.TYPE_INT_RGB);
		
		g2d = buffer.createGraphics();
		
		//currentTransform.translate(-viewX, -viewY);
		setTransform();
		//setTransform();
		
		panel = new MapPanel();
		this.add(panel);
		
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
					moveView(0,-1);
					break;
				case KeyEvent.VK_DOWN:
					moveView(0,1);
					break;
				case KeyEvent.VK_COMMA:
					viewSize += 1;
					break;
				case KeyEvent.VK_PERIOD:
					viewSize -= 1;
					break;
				}
				
				repaint();
			}
			
			
			
		});
		
	}
	
	private void setTransform()
	{
		//currentTransform.setToIdentity();
		double scale = (double)this.getHeight()/(viewSize*BLOCK_SIZE);
		//System.out.println("Scale = " + scale + "\nHeight = " + this.getHeight());
		
		currentTransform.setToIdentity();
		currentTransform.translate(this.getWidth()/2.0, this.getHeight()/2.0);
		currentTransform.scale(scale, scale);
		currentTransform.translate(-viewX*BLOCK_SIZE, -viewY*BLOCK_SIZE);	
	}
	
	public void setView(double x, double y)
	{
		viewX = trueMod(x, setup.getX());
		viewY = trueMod(y, setup.getY());
		repaint();
	}
	
	public void moveView(double dx, double dy)
	{
		setView(viewX+dx, viewY+dy);
	}
	
	private class MapPanel extends JPanel
	{
		public MapPanel()
		{
			
		}
		
		protected void paintComponent(Graphics g)
		{
			//super.paintComponent(g);

			g2d.setColor(spaceColor);
			g2d.setTransform(identity);
			g2d.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
			
			setTransform();
			g2d.setTransform(currentTransform);
			//g2d.translate(-viewX*BLOCK_SIZE, -viewY*BLOCK_SIZE);
			//g2d.setBackground(spaceColor);
			
			
			g2d.setColor(blockColor);
			//paintBlocks(g2d);
			
			
			for(int x= -1; x<=1;x++)
			{
				for (int y = -1; y<=1;y++)
				{
					g2d.setTransform(currentTransform);
					//g2d.translate(-viewX*BLOCK_SIZE, -viewY*BLOCK_SIZE);	
					
					g2d.translate(x*setup.getX()*BLOCK_SIZE, y*setup.getY()*BLOCK_SIZE);
					paintBlocks(g2d);
					
					
					//g2d.copyArea(0, 0, setup.getX()*BLOCK_SIZE, setup.getY()*BLOCK_SIZE,
					//		x*setup.getX()*BLOCK_SIZE, y*setup.getY()*BLOCK_SIZE);
					
					//g.drawImage(buffer, x*viewSize*BLOCK_SIZE, y*viewSize*BLOCK_SIZE, this);
				}
			}
			
			g.drawImage(buffer, 0, 0, this);
			
			
			/*
			double X = viewX-viewSize*BLOCK_SIZE/2.0;
			double Y = viewY-viewSize*BLOCK_SIZE/2.0;
			*/
			
			
			/*
			for(int x= -1; x<=1;x++)
			{
				for (int y = -1; y<=1;y++)
				{
					g2d.copyArea(viewX-viewSize*BLOCK_SIZE/2.0, viewY-viewSize*BLOCK_SIZE/2.0, viewSize*BLOCK_SIZE ,viewSize*BLOCK_SIZE
							, x*setup., arg5);
				}
			}
			
			
			Graphics2D g2 = (Graphics2D) g;		

			g2.setColor(spaceColor);
			//g2.setTransform(identity);
			g2.fillRect(0, 0, this.getWidth(), this.getHeight());
			g2.setTransform(currentTransform);
			*/

			
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
			if (block.isFilled())
				g2.fill(block.getShape());
			else
				g2.draw(block.getShape());
		}
	}
}
