package net.sf.jxpilot.graphics;

/**
 * Represents the various possible display modes of a JXPilotFrame.
 * @author Vlad
 *
 */
public enum DisplayMode {
	
	/**
	 * Full Screen Exclusive Mode
	 */
	FSEM,
	/**
	 * Undecorated Full Screen
	 */UFS, 
	 /**
	  * Almost Full Screen
	  */
	 AFS;
	
	/**
	 * Used for getting DisplayMode from Preferences.
	 */
	public static final String displayModeKey = "displayMode";	
	
	public static DisplayMode getDisplayMode(String value)
	{
		for(DisplayMode d : values())
		{
			if (d.toString().compareTo(value)==0)
			{
				return d;
			}
		}
		return null;
	}
}
