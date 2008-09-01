package net.sf.jxpilot.test;

/**
 * Class to hold ExtendedDrawables without constantly creating new instances. Might be better implemented as a LinkedList.
 * @author vlad
 *
 * @param <T>
 */
public class DrawableHandler<T extends ExtendedDrawable<T>> extends java.util.ArrayList<T>
{
	/**
	 * Represents the 1+the largest index used in the ArrayList.
	 * (The number of indexes used).
	 */
	private int size;
	
	/**
	 * A starter ExtendedDrawable that allows for new instances.
	 */
	private T starter;
	
	public DrawableHandler(T starter, int start_size)
	{
		super(start_size);
		
		//super();
		this.starter = starter;
		for (int i = 0;i<start_size;i++)
		{
			this.add(starter.getNewInstance());
		}		
		clearDrawables();
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
	public void clearDrawables()
	{
		size = 0;
	}
	
	
	/**
	 * Simply calls clearDrawables(). Overrides clear() in ArrayList.
	 */
	@Override
	public void clear()
	{
		clearDrawables();
	}
	
	/**
	 * Adds a Drawable to this DrawableHolder.
	 */
	
	public void addDrawable(T drawable)
	{
		if (size < super.size())
		{
			drawable.set(this.get(size));
		}
		else
		{
			T temp = starter.getNewInstance();
			temp.set(drawable);
			this.add(temp);
			System.out.println("Increasing Drawables size to " + (size+1));
		}
		size++;
	}
	
	
	/**
	 * Adds a Drawable to this DrawableHolder.
	 */
	public void addDrawable(Holder<? super T>  drawable)
	{
		if (size < super.size())
		{
			drawable.set(this.get(size));
		}
		else
		{
			T temp = starter.getNewInstance();
			drawable.set(temp);
			this.add(temp);
			System.out.println("Increasing Drawables size to " + (size+1));
		}
		size++;
	}
}
