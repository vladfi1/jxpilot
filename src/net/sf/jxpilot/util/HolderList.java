package net.sf.jxpilot.util;


/**
 * Class to hold ExtendedDrawables without constantly creating new instances. 
 * Might be better implemented as a LinkedList.
 * @author vlad
 *
 * @param <T>
 * @param <G>
 */
public class HolderList<G extends Holder<G>, T extends G> extends java.util.ArrayList<T>
{
	/**
	 * Represents the 1+the largest index used in the ArrayList.
	 * (The number of indexes used).
	 */
	private int size;
	
	/**
	 * A factory to generate new instances.
	 */
	private Factory<? extends T> factory;
	
	public HolderList(Factory<? extends T> factory, int start_size)
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
	
	public boolean add(G holder)
	{	
		if (size < super.size())
		{
			//holder.set(this.get(size));
			this.get(size).setFrom(holder);
		}
		else
		{
			T temp = factory.newInstance();
			temp.setFrom(holder);
			super.add(temp);
			System.out.println("Increasing holderlist size to " + (size+1));
		}
		size++;
		return true;
	}
	
	/*
	public boolean add(Holder<? super T> holder)
	{
		if (size < super.size())
		{
			//holder.set(this.get(size));
			this.get(size).setFrom(holder);
		}
		else
		{
			T temp = factory.newInstance();
			temp.setFrom(holder);
			super.add(temp);
			System.out.println("Increasing holderlist size to " + (size+1));
		}
		size++;
		return true;
	}
	*/
}