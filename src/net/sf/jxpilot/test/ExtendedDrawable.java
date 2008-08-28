package net.sf.jxpilot.test;

public interface ExtendedDrawable<T extends ExtendedDrawable<T>> extends Drawable
{
	/**
	 * Sets this ExtendedDrawable have the same state as other.
	 * @param other ExtendedDrawable.
	 */
	public abstract void setDrawable(T other);
	
	/**
	 * @return A new instance of this class.
	 */
	public abstract T getNewInstance();
}