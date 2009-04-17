package net.sf.jxpilot.graphics;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

/**
 * Text box implementation with default key presses.
 * @author Vlad Firoiu
 */
public class DefaultTextBox extends TextBox implements KeyListener {
	public static final int DEFAULT_LENGTH = 80;
	
	public DefaultTextBox(int max_length) throws IllegalArgumentException {super(max_length);}
	public DefaultTextBox() {this(DEFAULT_LENGTH);}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_DELETE) super.delete();
	}
	
	@Override
	public void keyReleased(KeyEvent e) {}
	
	@Override
	public void keyTyped(KeyEvent e) {
		System.out.println(e.getKeyChar());
		super.add(e.getKeyChar());
	}
	
}
