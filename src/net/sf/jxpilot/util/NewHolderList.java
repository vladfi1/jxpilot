package net.sf.jxpilot.util;

import java.util.ArrayList;

/**
 * Holds a list of {@code Holder<? super E>} objects to store data
 * without constantly creating new objects, thus avoiding costly 
 * garbage collection.
 * 
 * @author Vlad Firoiu
 *
 * @param <E> The element type.
 */
public class NewHolderList<E extends Holder<? super E>> extends ArrayList<E> {

	/**
	 * The default number of elements in a {@code HolderList}.
	 */
	public static final int DEFAULT_SIZE = 10;
	
	/**
	 * Default serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Used to generate new holders.
	 */
	private Factory<? extends E> factory;
	
	/**
	 * Actual number of elements in use.
	 */
	private int size = 0;
	
	public NewHolderList(Factory<? extends E> factory, int start_size) {
		super(start_size);
		this.factory = factory;
		for(int i = 0;i<start_size;i++) {
			super.add(factory.newInstance());
		}
	}
	
	public NewHolderList(Factory<? extends E> factory) {
		this(factory, DEFAULT_SIZE);
	}
	
	/**
	 * Returns the actual number of elements used in this {@code HolderList}.
	 */
	@Override
	public int size() {
		return size;
	}
	
	/**
	 * This method sets the size to zero, but does not actually clear any data.
	 */
	@Override
	public void clear() {
		size = 0;
	}
	
	/**
	 * Stores the specified data into the list.
	 * @param holder Containing the data to store.
	 * @return The holder storing the data.
	 */
	public E add(Holder<? super E> holder) {
		if(size==super.size()) {
			super.add(factory.newInstance());
			System.out.println("Increased HolderList size to " + (size+1));
		}
		E temp = super.get(size++);
		//holder.set(temp);
		temp.setFrom(holder);
		return temp;
	}
	
	@Override
	public boolean add(E element) {
		add((Holder<? super E>)element);
		return true;
	}
}
