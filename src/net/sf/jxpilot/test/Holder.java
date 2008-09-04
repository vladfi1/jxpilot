package net.sf.jxpilot.test;

/**
 * 
 * @author vlad
 *
 * @param <T> The subclass type.
 */
public interface Holder<T extends Holder<T>> {
	/**
	 * @param other Sets another object to have the same information as this Holder.
	 */
	public void set(T other);
}
