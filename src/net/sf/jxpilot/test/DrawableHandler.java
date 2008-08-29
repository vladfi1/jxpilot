package net.sf.jxpilot.test;

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
	 * Adds a Drawable to this DrawableHolder.

	 * @param x The x position of the Drawable.
	 * @param y The y position of the Drawable.
	 */
	public void addDrawable(T drawable)
	{
		if (size < super.size())
		{
			this.get(size).setDrawable(drawable);
		}
		else
		{
			T temp = starter.getNewInstance();
			temp.setDrawable(drawable);
			this.add(temp);
			System.out.println("Increasing Drawables size to " + (size+1));
		}
		size++;
	}	
}
