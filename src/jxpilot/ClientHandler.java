package XPilot;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable
{
	private Object wait = new Object();//used to wait
	
	XPilotServer server;
	public static final int port = 8189;
	private ServerSocket s;
	boolean ServerFull = false;
	private boolean connected = false;
	
	public ClientHandler()
	{
		try
		{
			s = new ServerSocket(port);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public ClientHandler(XPilotServer x)
	{
		this();
		server = x;
	}
	

	public void send()
	{
		if (connected)
		synchronized (wait)
		{
			//System.out.println("Client Handler: Sending register");
			wait.notifyAll();
		}
	}
	
	public void run()
	{
		while(true)
		{
			try
			{
				System.out.println("ClientHandler: waiting for client");
				Socket incoming = s.accept();
				
				//ObjectOutputStream objOut = new ObjectOutputStream(incoming.getOutputStream());
				
				
				int x = server.addClient(XPilotServer.REMOTE);
				
				//objOut.writeObject(x);
				if (x>=0)
				{
					System.out.println("Client handler: added new Client Interface");
					//ClientInterface c = new ClientInterface(x, incoming);
					//c.run();
					Thread t = new ClientInterface(x, incoming);
					t.start();
					connected = true;
				}
				else
				{
					System.out.println("Client Interface: can't get register from server!");
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	class ClientInterface extends Thread
	{
		Socket incoming=null;
		private ObjectOutputStream objOut;
		private ObjectInputStream objIn;
		private XPilotRegister register=null;
		private ClientInput input = null;
		private boolean send=true;
		int number;
		
		ClientInterface()
		{
			
		}
		
		ClientInterface(XPilotRegister r)
		{
			register = r;
		}
		
		ClientInterface(Socket s)
		{
			incoming = s;
		}
		
		ClientInterface(XPilotRegister r,Socket s)
		{
			register = r;
			incoming = s;
		}
		
		ClientInterface(int i,Socket s)
		{
			number = i;
			incoming = s;
			register = server.getRegister(i);
			input = (ClientInput) server.getClient(i);
		}
		
		public void run()
		{
			System.out.println("ClientInterface: running!");
			try
			{
				System.out.println("Client connected to server!");
				
				objOut = new ObjectOutputStream(incoming.getOutputStream());
				objIn = new ObjectInputStream(incoming.getInputStream());
			
				ClientInputReciever reciever = new ClientInputReciever();
				Thread thread = new Thread(reciever);
				thread.start();
				
				synchronized (register)
				{
					objOut.writeObject(register);
				}
				
				while(true)
				{
					if (incoming!=null)
					{
						objOut.reset();				
						
						synchronized (register)
						{
							objOut.writeObject(register);
						}
						
						//System.out.println("Sent register!");
						
						//synchronized (input)
						//{
							//input.setInput((ClientInput)objIn.readObject());
						//}
						//System.out.println("Recieved input!");
					}
					
					synchronized (wait)
					{
						wait.wait();
						//System.out.println("stopped waiting");
					}
					
				}
			
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	
		public void sendRegister()
		{
			if (incoming!=null)
			{
				try
				{
					objOut.reset();
					objOut.writeObject(register);

				}
				catch (Exception e)
				{
					System.out.println("Server error: send Register");
					e.printStackTrace();
				}
			}
		}
		
		class ClientInputReciever implements Runnable
		{
			public void run()
			{
				try
				{
				while(true)
					{
						synchronized (input)
						{
							input.setInput((ClientInput)objIn.readObject());
						}
					}
				}
				catch (Exception e)
				{
					server.removeClient(number);
					e.printStackTrace();
				}
			}
		}
	}
}