package net.sf.jxpilot;

import javax.swing.*;
import java.awt.*;

import javax.swing.Timer;
import java.awt.event.*;

import java.awt.geom.*;

import java.awt.image.BufferedImage;

public class XPilotClient implements XPClient
{
	private final int FPS = 20;
	
	private String name="Vlad";
	boolean local=false;
	
	private XPilotServer server = null;
	private Timer timer;
	private XPilotRegister register;
	boolean close = false;
	//private Point2D center;
	//private MiniMap map;
	private AffineTransform identity = new AffineTransform();
	
	Object sender;
	
	private boolean mouseHandling = true;
	private boolean turnsRight = false;
	private boolean turnsLeft = false;
	
	private double MouseX;
	private double NewX;
	private double MouseTurn=0;
	private boolean FirstTurn = true;//used to set first ship turn
	private double TurnSpeed = 1;
	private double Width;
	private double Height;
	private boolean Thrusts=false;
	private boolean Fires = false;
	private boolean minimap=true;
	private double miniX=200;
	private double miniY=200;
	private Rectangle2D.Double MiniMap;
	private double scale;
	private int minikey=KeyEvent.VK_M;
	private int infoX = 20;
	
	private boolean attaches=false;
	private boolean detaches=false;
	private int attach=KeyEvent.VK_CONTROL;
	private int detach=KeyEvent.VK_Z;
	
	private double keyboardTurnSpeed = 20;
	private int leftTurn = KeyEvent.VK_A;
	private int rightTurn = KeyEvent.VK_S;
	private int switchInputs = KeyEvent.VK_K;
	private int keyThrust = KeyEvent.VK_SHIFT;
	private int keyShoot = KeyEvent.VK_ENTER;
	
	private int switchTeams = KeyEvent.VK_F5;
	private boolean switches=false;
	
	private boolean takes = false;
	private int take = KeyEvent.VK_H;
	
	private boolean pauses = false;
	private int pause = KeyEvent.VK_PAUSE;
	
	private Color SPACE = Color.BLACK;
	private Color BORDER = Color.RED;
	private Color SHIP = Color.WHITE;
	private Color SHIELD = Color.ORANGE;
	private Color WALL = Color.BLUE;
	private Color BULLET = Color.WHITE;
	private Color IMMUNE = Color.BLUE;
	private Color BALL = Color.GREEN;
	private Color BOX = Color.WHITE;
	private Color CONNECTING = Color.YELLOW;
	private Color NAME = Color.WHITE;
	private Color BASE = Color.WHITE;
	
	private void init(XPilotRegister r)
	{
		register = new XPilotRegister(r);
		MiniMap = new Rectangle2D.Double();
		
		double m = register.Border.getHeight()/register.Border.getWidth();
		
		if (m>miniY/miniX)
		{
			MiniMap.setFrame(0, 0, miniY/m, miniY);
			scale = miniY/register.Border.getHeight();
		}else
		{
			MiniMap.setFrame(0,0,miniX, miniX*m);
			scale = miniX/register.Border.getWidth();
		}
		//center = new Point2D.Double(0,0);
		
		//starts loop to display

		timer = new Timer((int)1000/FPS,new ClientTimer());
		timer.start();
	}
	
	public XPilotClient(XPilotRegister r)
	{
		init(r);
	}
	
	public XPilotClient(XPilotRegister r, Object sender)
	{
		this(r);
		this.sender =sender;
	}
	
	public XPilotClient(XPilotRegister r, boolean local)
	{
		this.local =local;
		init(r);
	}
	
	public XPilotClient(XPilotRegister r, XPilotServer x)
	{
		local = true;
		server = x;
		init(r);
	}
	
	public void setRegister(XPilotRegister r)
		{register=r;}
	
	public XPilotRegister getRegister() {return register;}
	
	public double turnShip() 
	{
		if (mouseHandling)
		{
			MouseTurn = NewX - MouseX;
			MouseX = NewX;
			return MouseTurn*TurnSpeed;
		}
		else
		{
			if (turnsRight)
			return TurnSpeed * keyboardTurnSpeed;
			else if (turnsLeft) return -TurnSpeed * keyboardTurnSpeed;
			
			return 0;
		}
	}
	
	public boolean Thrusts() {return Thrusts;}
	
	public boolean Fires()
	{
		return Fires;
	}
	
	public boolean Attaches(){return (attaches&&!detaches);}
	public boolean Detaches(){return detaches;}
	
	public String getName(){return name;}
	
	public boolean switchTeams()
	{
		if (switches)
		{
			switches = false;
			return true;
		}
		
		return false;
		
	}
	
	public boolean isRobot() {return false;}
	
	public boolean takeBase(){return takes;}
	
	public boolean pauses()
	{
		if (pauses)
		{
			pauses = false;
			return true;
		}
		return false;
	}
	
	public void close()
	{
		close = true;
	}
	
	class ClientTimer implements ActionListener
	{
		private ClientFrame frame;
		BufferedImage buffer;
		Graphics2D g2d;
		
		ClientTimer()
		{
			frame = new ClientFrame();
			
			if (!local)
			{
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
			else frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			
			frame.setVisible(true);
			
			buffer = new BufferedImage(frame.getWidth(),frame.getHeight(),BufferedImage.TYPE_INT_RGB);
			g2d = buffer.createGraphics();
		}
		
		public ClientFrame getFrame(){return frame;}
		
		private int ImageWidth(int i)
		{
			if (i%4==0) {return 0;}
			else if (i<4) {return 1;}
			else {return -1;}
		}
		
		private int ImageHeight(int i)
		{
			if (i%4==2||i==8) {return 0;}
			else if ((i+1)%8<4) {return 1;}
			else {return -1;}
		}
		
		//actual display loop
		public void actionPerformed(ActionEvent e)
		{
			Width = frame.getWidth();
			Height = frame.getHeight();
			//center.setLocation(Width/2, Height/2);
			
			g2d.setTransform(identity);
			
			g2d.setColor(SPACE);
			g2d.fillRect(0, 0, (int)Width, (int)Height);
			
			for (int i =0;i<9;i++)
			{
				g2d.setTransform(identity);
				g2d.translate(-register.Focus.getX(), -register.Focus.getY());
				g2d.translate(Width/2, Height/2);
				g2d.translate(register.Border.getWidth()*ImageWidth(i),
						register.Border.getHeight()*ImageHeight(i));
				
				drawRegister(g2d, register);
			}
			
			//draw minimap
			if (minimap)
			{
			g2d.setTransform(identity);
			g2d.scale(scale, scale);
			//g2d.translate(-register.Border.getMinX(), -register.Border.getMinY());
			
			drawRegister(g2d,register);
			}
			
			drawGameInfo(g2d);
			
			frame.repaint();
			
			if (sender != null)
			{
				synchronized(sender)
				{
					sender.notifyAll();
				}
			}
			
			if (close)
			{
				timer.stop();
				if (!local)
					System.exit(0);
				else
					server.removeClient(register.self);
			}
		}
		
		//draws the register...
		public void drawRegister(Graphics2D g2, XPilotRegister r)
		{
			if (r != null)
			{
				//g2.setColor(SPACE);
				//g2.fill(r.Border);
				
				g2.setColor(BORDER);
				g2.draw(r.Border);
				
				for (Ship s : r.Ships)
				{
					
					//System.out.println(s.inUse());
					if(s.inUse())
						drawShip(g2, s);
					
				}
				g2.setColor(WALL);
				for (Line2D l : r.Lines)
				{g2.draw(l);}
				for (Ball b : r.Balls)
				{drawBall(g2, b);}
				
				for (Base b:r.Bases)
				{drawBase(g2,b);}
				
			}
		}
		
		private void drawShip(Graphics2D g2, Ship s)
		{
			if (s.paused()) return;
			
			Ship self = register.getSelf();
			
			if (! s.isDead())
			{
				if (self.getTeam()==s.getTeam() && self!= s)
				{
					g2.setColor(IMMUNE);
					g2.draw(s.getCircle());
				}
				else
				{
					if (s.isShielded())
					{
						g2.setColor(SHIELD);
						g2.draw(s.getCircle());
						g2.setColor(SHIP);
					}
					else
					{
						g2.setColor(SHIP);
						g2.draw(s.getCircle());
					}
				}

				//g2.draw(s.getPath());

				for (Line2D l : s.getLines())
				{g2.draw(l);}
			}
			
			for (Bullet b : s.getBullets())
			{
				if (b.isActive())
				{
				g2.setColor(BULLET);
				if ((s==register.getSelf() && !register.selfkill)
						|| (s!= register.getSelf() && s.getTeam()== register.getSelf().getTeam()))
					{
						g2.setColor(IMMUNE);
						//System.out.println("immune");
					}
				drawBullet(g2, b);
				}
			}
			
			g2.setColor(NAME);
			g2.drawString(s.name, (float)(s.getCenter().getX()-s.name.length()*2.8), 
					(float) (s.getCenter().getY()+s.getRadius()+10.0));
			
			if (s.spawning())
			{
				g2.drawString(String.valueOf(s.time_to_spawn()), (float)s.getCenter().getX(),
						(float)s.getCenter().getY());
			}
			
		}
		
		private void drawBase(Graphics2D g2, Base b)
		{
			g2.setColor(BASE);
			g2.draw(b.getLine());
			
			if (b.getName()!=null)
			{
				String s = b.getShip().name;
				g2.drawString(s, (float)(b.getBase().getX()-s.length()*2.8), (float)(b.getBase().getY()+12.0));
			}
			
		}
		
		private void drawBullet(Graphics2D g2, Bullet b)
		{
			g2.fill(b.getCircle());
			//g2.draw(b.getPath());
		}
		
		private void drawBall(Graphics2D g2, Ball b)
		{
			g2.setColor(BALL);
			g2.fill(b.getCircle());
			//g2.draw(b.getPath());
			if (b.isConnecting())
				g2.setColor(CONNECTING);
			else
				g2.setColor(BALL);
			g2.draw(b.getConnector());
			
			
			g2.setColor(BOX);
			g2.draw(b.getBox());
			
			//g2.draw(b.getPath());
			//System.out.println(b.getConnector()==null);
			//System.out.println(b.getConnector().getX1()-b.getConnector().getX2());
		}
		
		private void drawGameInfo(Graphics2D g2)
		{
			g2.setColor(Color.WHITE);

			register.gameInfo.drawInfo(g2, infoX, miniY+infoX);
		}
		
		//inner classes for graphics
		class ClientFrame extends JFrame
			{
				private ClientPanel panel;
			
				ClientFrame()
				{
					this.setExtendedState(this.MAXIMIZED_BOTH);
					//this.setSize(0, 0);
					this.setTitle("XPilot");
					
					Width = this.getWidth();
					Height = this.getHeight();
					
					panel = new ClientPanel();
					this.add(panel);
					addKeyListener(new KeyHandler());
					this.addWindowListener(new WindowHandler());
				}
				
				//handles keyboard input
				private class KeyHandler implements KeyListener
				{
					int k;
					
					public void keyPressed(KeyEvent e)
					{
						k = e.getKeyCode();
						if(k==minikey)
						{
							minimap=!minimap;
						}
						else if(k==attach)
						{
							attaches=true;
							detaches=false;
							//System.out.println("ATTACHING");
						}
						else if (k==detach)
						{
							detaches=true;
						}
						else if (k==switchTeams)
						{
							switches=true;
						}
						else if (k==take)
						{
							takes = true;
						}
						else if (k==pause)
						{
							pauses = true;
						}
						else if (k==rightTurn)
						{
							turnsRight = true;
							turnsLeft = false;
						}
						else if (k==leftTurn)
						{
							turnsRight = false;
							turnsLeft = true;
						}
						else if (k==switchInputs)
						{
							mouseHandling  = !mouseHandling;
						}
						else if (k == keyThrust && !mouseHandling)
						{
							Thrusts = true;
						}
						else if (k==keyShoot && !mouseHandling)
						{
							Fires = true;
						}
					}
					
					public void keyReleased(KeyEvent e)
					{
						k = e.getKeyCode();
						if(k==attach)
						{
							attaches=false;
						}
						else if (k==detach)
						{
							detaches=false;
						}
						else if (k==switchTeams)
						{
							switches=false;
						}
						else if (k==take)
						{
							takes = false;
						}
						else if (k==pause)
						{
							pauses = false;
						}
						else if (k==rightTurn)
						{
							turnsRight = false;
						}
						else if (k==leftTurn)
						{
							turnsLeft = false;
						}
						else if (k == keyThrust && !mouseHandling)
						{
							Thrusts = false;
						}
						else if (k==keyShoot && !mouseHandling)
						{
							Fires = false;
						}
							
					}
					public void keyTyped(KeyEvent e){}
				}
				
				class WindowHandler extends WindowAdapter
				{
					public void windowActivated(WindowEvent e){}
					public void windowOpened(WindowEvent e){}
					
					public void windowClosed(WindowEvent e)
					{
						close = true;
					}
					
					public void windowClosing(WindowEvent e)
					{
						close = true;
					}
					
				}
				
				class ClientPanel extends JPanel
				{
					ClientPanel()
					{
						setSize(ClientFrame.this.getWidth(), ClientFrame.this.getHeight());
						addMouseListener(new MouseHandler());
						addMouseMotionListener(new MouseMotionHandler());

					}
					
					//actual graphics function
					public void paintComponent(Graphics g)
					{
						g.drawImage(buffer, 0, 0, this);
					}
					
					private class MouseHandler extends MouseAdapter
					{
						public void mousePressed(MouseEvent e)
						{
							if (e.getButton()==e.BUTTON3)
							{Thrusts = true;}
							else if (e.getButton()==e.BUTTON1)
							{Fires = true;}
						}
						public void mouseReleased(MouseEvent e)
						{
							if (e.getButton()== e.BUTTON3)
							{Thrusts = false;}
							else if (e.getButton()==e.BUTTON1)
							{Fires = false;}
						}
					}
					
					private class MouseMotionHandler extends MouseMotionAdapter
					{
						public void mouseMoved(MouseEvent e)
						{
							if (FirstTurn)
							{
								MouseX = e.getX();
								NewX = MouseX;
								FirstTurn = false;
							}
							else
							{
								NewX = e.getX();
							}
						}
						public void mouseDragged(MouseEvent e)
						{
							if (FirstTurn)
							{
								MouseX = e.getX();
								NewX = MouseX;
								FirstTurn = false;
							}
							else
							{
								NewX = e.getX();
							}
						}
					}
				}
			}
		}	
}

/*
class MiniMap
{
	ArrayList<Line2D> lines;
	ArrayList<Point2D> points;
	Dimension2D size;
	
	MiniMap(XPilotRegister r, Dimension2D aSize)
	{
		lines = new ArrayList<Line2D>(r.Lines.size());
		points = new ArrayList<Point2D>(r.Ships.size());
		for (int i =0; i<r.Lines.size();i++)
		{
			lines.add(i, new Line2D.Double(0,0,0,0));
		}
		for (int i =0; i<r.Ships.size();i++)
		{
			points.add(i, new Point2D.Double(0,0));
		}
		
		size = aSize;
		
		setMiniMap(r);
	}
	
	private void setMiniMap(XPilotRegister r)
	{
		for (int i =0;i<lines.size();i++)
		{
			lines.get(i).setLine(r.Lines.get(i));
		}
		for (int i=0;i<points.size();i++)
		{
			points.get(i).setLocation(r.Ships.get(i).getCenter());
		}
		for (Line2D l : lines)
		{
			l.setLine(l.getX1()/size.getWidth(), l.getY1()/size.getHeight(),
					l.getX2()/size.getWidth(), l.getY2()/size.getHeight());
		}
		for (Point2D p:points)
		{
			p.setLocation(p.getX()/size.getWidth(), p.getY()/size.getHeight());
		}
	}
}
*/