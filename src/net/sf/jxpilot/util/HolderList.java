package net.sf.jxpilot.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Holds a list of {@code Holder<? super E>} objects to store data
 * without constantly creating new objects, thus avoiding costly 
 * garbage collection.
 * 
 * @author Vlad Firoiu
 *
 * @param <E> The element type.
 */
public class HolderList<E extends Holder<? super E>> implements List<E> {
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
	 * Actual storage of holder objects.
	 */
	private ArrayList<E> list;
	
	/**
	 * Actual number of elements in use.
	 */
	private int size = 0;
	
	public HolderList(Factory<? extends E> factory, int start_size) {
		list = new ArrayList<E>(start_size);
		this.factory = factory;
		for(int i = 0;i<start_size;i++) {
			list.add(factory.newInstance());
		}
	}
	
	public HolderList(Factory<? extends E> factory) {
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
		if(size==list.size()) {
			list.add(factory.newInstance());
			System.out.println("Increased HolderList size to " + (size+1));
		}
		E temp = list.get(size++);
		//holder.set(temp);
		temp.setFrom(holder);
		return temp;
	}
	
	@Override
	public boolean add(E element) {
		this.add((Holder<? super E>)element);
		return true;
	}

	@Override
	public void add(int index, E element) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public E get(int index) {
		return list.get(index);
	}

	@Override
	public int indexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public Iterator<E> iterator() {
		return new HolderIterator();
	}

	private class HolderIterator implements Iterator<E> {
		int index = 0;
		public boolean hasNext(){return index<size();}
		@Override
		public E next() {
			return list.get(index++);
		}
		@Override
		public void remove() {throw new UnsupportedOperationException();}
	}
	
	@Override
	public int lastIndexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ListIterator<E> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public E remove(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public E set(int index, E element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
