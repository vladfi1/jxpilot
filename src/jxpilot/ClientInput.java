package jxpilot;

public class ClientInput implements XPClient, java.io.Serializable
{
	private String name="";
	private double turn = 0;
	private boolean thrusts=false;
	private boolean detaches = false;
	private boolean attaches = false;
	private boolean switches = false;
	private boolean fires = false;
	private boolean takes = false;
	private boolean pauses = false;
	
	public String getName(){return name;}
	public void setRegister(XPilotRegister r){}
	public XPilotRegister getRegister(){return null;}
	public boolean Thrusts(){return thrusts;}
	public double turnShip(){return turn;}
	public boolean Detaches(){return detaches;}
	public boolean Attaches(){return attaches;}
	public boolean Fires(){return fires;}
	public boolean switchTeams(){return switches;}
	public boolean isRobot(){return false;}
	public boolean takeBase(){return takes;}
	public boolean pauses(){return pauses;}
	
	public synchronized void setInput(XPClient other)
	{
		name = other.getName();
		turn = other.turnShip();
		thrusts = other.Thrusts();
		detaches = other.Detaches();
		attaches = other.Attaches();
		switches = other.switchTeams();
		fires = other.Fires();
		takes = other.takeBase();
		pauses = other.pauses();
	}
}
