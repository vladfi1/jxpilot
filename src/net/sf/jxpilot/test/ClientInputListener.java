package net.sf.jxpilot.test;

/**
 * Interface for listening to the possible user inputs.
 * @author vlad
 */
public interface ClientInputListener
{
	/**
	 * Signals that client wants to quit the game.
	 */
	public void quit();
	
	/**
	 * @param key The constant from the class Keys corresponding to this key.
	 * @param value Whether or not the key is pressed.
	 */
	public void setKey(int key, boolean value);
}
