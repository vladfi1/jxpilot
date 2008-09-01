package net.sf.jxpilot.test;

import javax.swing.*;
import net.sf.xpilotpanel.XPilotPanel;
import net.sf.xpilotpanel.gui.AboutWindow;

import java.net.*;
import java.awt.Image;
import java.util.HashMap;

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
            xpilotpanelEmbeddedParams.putByte(
                    XPilotPanel.EMBEDDED_PARAMETER_MAIN_WINDOW_TITLE,
                    "JXPilot - XPilotPanel");
            xpilotpanelEmbeddedParams.putByte(XPilotPanel.EMBEDDED_PARAMETER_ICON,
                    getJXPilotIcon());
            xpilotpanelEmbeddedParams.putByte(
                    XPilotPanel.EMBEDDED_PARAMETER_CLIENT_LAUNCH_METHOD,
                    UDPTest.class.getDeclaredMethod("runClient", String.class,
                            int.class));

            XPilotPanel.embeddedLaunch(xpilotpanelEmbeddedParams);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
		*/
		
        runClient(SERVER_IP_ADDRESS, SERVER_MAIN_PORT);
	}
	
	public static void runClient(String serverIP, int serverPort)
	{	
		new Client().runClient(serverIP, serverPort);
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