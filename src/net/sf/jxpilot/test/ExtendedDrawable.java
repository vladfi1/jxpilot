package net.sf.jxpilot.test;

public interface ExtendedDrawable<T extends ExtendedDrawable<T>> extends Drawable
{
	/**
	 * Sets this ExtendedDrawable have the same state as other.
	 * @param other ExtendedDrawable.
	 * @return This ExtendedDrawable.
	 */
	public abstract T setDrawable(T other);
	
	/**
	 * @return A new instance of this class.
	 */
	public T getNewInstance();
}