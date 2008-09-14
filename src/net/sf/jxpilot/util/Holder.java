package net.sf.jxpilot.util;

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
	
	/**
	 * Sets this Holder to have the same data as other.
	 * @param other
	 */
	//public void setFrom(T other);
	
	public void setFrom(Holder<T> other);
}
