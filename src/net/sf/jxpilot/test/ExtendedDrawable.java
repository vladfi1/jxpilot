package net.sf.jxpilot.test;

/**
 * Provides new drawable instances. Subclasses should be of the form Foo implements ExtendedDrawable<Foo>
 * @author vlad
 *
 * @param <T>
 */
public interface ExtendedDrawable<T extends ExtendedDrawable<T>> extends Drawable
{
	/**
	 * @return A new instance of this class.
	 */
	public abstract T getNewInstance();
	/**
	 * @param other Sets another drawable to have the same information as this drawable.
	 */
	public void set(T other);
}