package net.sf.jxpilot.util;


/**
 * Class to hold ExtendedDrawables without constantly creating new instances. Might be better implemented as a LinkedList.
 * @author vlad
 *
 * @param <T>
 */
public class HolderList<T extends Holder<? super T>> extends java.util.ArrayList<T>
{
	/**
	 * Represents the 1+the largest index used in the ArrayList.
	 * (The number of indexes used).
	 */
	private int size;
	
	/**
	 * A factory to generate new instances.
	 */
	private Factory<T> factory;
	
	public HolderList(Factory<T> factory, int start_size)
	{
		super(start_size);
		
		//super();
		this.factory = factory;
		for (int i = 0;i<start_size;i++)
		{
			super.add(factory.newInstance());
		}		
		this.clear();
	}
	
	/**
	 * Returns the number of elements currently used.
	 * 
	 * @override size() in ArrayList
	 */
	@Override
	public int size()
	{
		return size;
	}
	
	/**
	 * De-activates all the Drawables in this ArrayList. This method only sets the size to 0,
	 * it does not actually set the Drawables to inactive to save on time.
	 */
	@Override
	public void clear()
	{
		size = 0;
	}
	
	@Override
	public boolean add(T holder)
	{
		if (size < super.size())
		{
			holder.set(this.get(size));
		}
		else
		{
			T temp = factory.newInstance();
			temp.set(holder);
			super.add(temp);
			System.out.println("Increasing holderlist size to " + (size+1));
		}
		size++;
		return true;
	}
	
	
	public void add(Holder<? super T>  holder)
	{
		if (size < super.size())
		{
			holder.set(this.get(size));
		}
		else
		{
			T temp = factory.newInstance();
			holder.set(temp);
			super.add(temp);
			System.out.println("Increasing holderlist size to " + (size+1));
		}
		size++;
	}
}
