package net.sf.jxpilot.test;

/**
 * Subclasses should be of the form Foo implements Holder<Foo>
 * @author vlad
 *
 * @param <T> The subclass type.
 */
public interface Holder<T> {
	/**
	 * @param other Sets another object to have the same information as this Holder.
	 */
	public void set(T other);
}
