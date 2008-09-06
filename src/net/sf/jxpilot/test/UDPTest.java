package net.sf.jxpilot.test;

import net.sf.xpilotpanel.XPilotPanel;
import net.sf.xpilotpanel.gui.AboutWindow;
import net.sf.xpilotpanel.preferences.Preferences;

import java.awt.Image;
import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;

public class UDPTest {

	/**
	 * Whether or not to print various packet information.
	 */
	public static final boolean PRINT_PACKETS = false;
	
	/**
	 * Whether or not to print reliable data.
	 */
	public static final boolean PRINT_RELIABLE = false;
	
	public static final short SERVER_MAIN_PORT = 15345;
	//public static final short CLIENT_PORT = 15345;
	
	public static final String LKRAUSS_ADDRESS = "213.239.204.35";
	public static final String STROWGER_ADDRESS = "149.156.203.245";
	public static final String CHAOS_ADDRESS = "129.13.108.207";
	public static final String BURKEN_ADDRESS = "83.168.206.7";
	public static final String LOCAL_LOOPBACK_ADDRESS = "127.0.0.1";
	public static final String SERVER_IP_ADDRESS = LKRAUSS_ADDRESS;
	
	/**
     * JXPilot's windows icon.
     */
    private static Image icon = null;
		
	public static void main(String[] args)
	{
		/*
        try {
            java.util.Map<String, Object> xpilotpanelEmbeddedParams = new HashMap<String, Object>();
            xpilotpanelEmbeddedParams.put(
                    XPilotPanel.EMBEDDED_PARAMETER_MAIN_WINDOW_TITLE,
                    "JXPilot - XPilotPanel");
            xpilotpanelEmbeddedParams.put(XPilotPanel.EMBEDDED_PARAMETER_ICON,
                    getJXPilotIcon());
            xpilotpanelEmbeddedParams.put(
                    XPilotPanel.EMBEDDED_PARAMETER_CLIENT_LAUNCH_METHOD,
                    UDPTest.class.getDeclaredMethod("runClient", String.class,
                            int.class, Preferences.class));

            XPilotPanel.embeddedLaunch(xpilotpanelEmbeddedParams);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
		*/
		
        runClient(SERVER_IP_ADDRESS, SERVER_MAIN_PORT, null);
	}
	
	public static void runClient(String serverIP, int serverPort, Preferences prefs)
	{	
		new Client(prefs).runClient(serverIP, serverPort);
	}
	
    /**
     * Returns JXPilot's windows icon, retrieved from file and stored as
     * 'single'.
     * 
     * @return JXPilot icon.
     */
    public static Image getJXPilotIcon() {
        if (icon == null) {
            URL u = AboutWindow.class.getClassLoader().getResource(
                    "data/JXPilotIcon.png");
            icon = new ImageIcon(u).getImage();
        }
        
        return icon;
    }
}
