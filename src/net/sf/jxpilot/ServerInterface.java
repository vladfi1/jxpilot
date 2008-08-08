package net.sf.jxpilot;

import java.io.*;

public class ServerInterface implements Runnable
{
	private volatile boolean quit = false;
	private XPilotServer server;
	
	private String options = "Connect local client: local\n Start robot client: robot";
	public static final String robot = "robot";
	public static final String local = "local";
	public static final String remove_robots = "remove all";
	public static final String remove_robot = "remove next";
	
	BufferedReader reader;
	
	public ServerInterface(XPilotServer x)
	{
		server = x;
		reader = new BufferedReader(new InputStreamReader(System.in));
	}
	
	public void run()
	{
		while (!quit)
		{
			try
			{
				System.out.println(options);
				processInput(reader.readLine());
			}
			catch(Exception e) {
				e.printStackTrace();
			}

		}
	}
	
	private void processInput(String input)
	{
		if (input.matches(robot))
		{
			server.addClient(server.ROBOT);
		}
		else if (input.matches(local))
		{
			server.addClient(server.LOCAL);
		}
		else if (input.matches(remove_robots))
		{
			server.removeRobots();
		}
		else if (input.matches(remove_robot))
		{
			server.removeNextRobot();
		}
		else
		{
			System.out.println("Invalid Input!");
		}
	}
	
	public void quit(){quit = true;}
}
