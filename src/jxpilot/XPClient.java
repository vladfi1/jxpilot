package XPilot;

public interface XPClient
{
	public void setRegister(XPilotRegister r);
	public XPilotRegister getRegister();
	public double turnShip();
	public boolean Thrusts();
	public boolean Fires();
	//public Point2D getCenter();
	public boolean Attaches();
	public boolean Detaches();
	public String getName();
	public boolean switchTeams();
	public boolean isRobot();
	public boolean takeBase();
	public boolean pauses();
}
