package net.sf.jxpilot.test;

import java.awt.*;
import java.awt.geom.*;
import static net.sf.jxpilot.test.BlockMapSetup.*;
import static net.sf.jxpilot.test.MapBlock.BLOCK_SIZE;

public class Base extends BaseHolder implements Drawable
{
	public static final Color BASE_COLOR = Color.WHITE;
	
	protected final int team;
	protected final BaseType base_type;
	/**
	 * Block coordinates.
	 */
	protected final int x, y;
	protected final Line2D baseShape;
	
	protected Base(int num, int team, BaseType base_type, int x, int y)
	{
		super.num = num;
		this.team = team;
		this.base_type = base_type;
		this.x = x;
		this.y = y;
		baseShape = getBaseShape(base_type);
	}
	
	protected Base(byte block_type, int num, int x, int y)
	{
		//this(num, block_type-(BaseType.getBaseType(block_type).getBlockType()), BaseType.getBaseType(block_type), x, y);
		
		BaseType base_type = BaseType.getBaseType(block_type);

		int team = block_type-base_type.getBlockType();
		
		super.num = num;
		this.team = team;
		this.base_type = base_type;
		this.x=x;
		this.y=y;
		this.baseShape = getBaseShape(base_type);
	}
	
	/**
	 * 
	 * @param block_type
	 * @param num
	 * @param x
	 * @param y
	 * @return The base with the given data, or null if block_type is invalid.
	 */
	public static Base createBase(byte block_type, int num, int x, int y)
	{
		BaseType base_type = BaseType.getBaseType(block_type);
		
		if(base_type==null) return null;
		
		int team = block_type-base_type.getBlockType();
		
		return new Base(num, team, base_type, x, y);
	}
	
	public int getTeam(){return team;}
	public BaseType getBaseType(){return base_type;}
	public int getX(){return x;}
	public int getY(){return y;}
	
	/**
	 * Note that this only draws the line, not any player names.
	 */
	@Override
	public void paintDrawable(Graphics2D g2d)
	{
		AffineTransform saved = g2d.getTransform();
		
		int x = this.x*BLOCK_SIZE;
		int y = this.y*BLOCK_SIZE;
		
		g2d.translate(x, y);
		g2d.setColor(BASE_COLOR);
		g2d.draw(baseShape);
		
		//g2d.translate(-x, -y);
		
		g2d.setTransform(saved);
	}
	
	private static Line2D getBaseShape(BaseType type)
	{
		Line2D line  = new Line2D.Float();
		
		switch(type)
		{
		case LEFT:
			line.setLine(BLOCK_SIZE, 0, BLOCK_SIZE, BLOCK_SIZE);
			break;
		case RIGHT:
			line.setLine(0, 0, 0, BLOCK_SIZE);
			break;
		case DOWN:
			line.setLine(0, BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
			break;
		case UP:
			line.setLine(0, 0, BLOCK_SIZE, 0);
			break;	
		}
		
		return line;
	}
	
	public static enum BaseType
	{
		RIGHT(SETUP_BASE_RIGHT), LEFT(SETUP_BASE_LEFT), UP(SETUP_BASE_UP), DOWN(SETUP_BASE_DOWN);
		
		private BaseType(byte block_type)
		{
			this.block_type = block_type;
		}
		
		private final byte block_type;
		
		public byte getBlockType(){return block_type;}
		
		/**
		 * 
		 * @param block_type A block from the map data.
		 * @return The corresponding base type.
		 */
		public static BaseType getBaseType(byte block_type)
		{
			for(BaseType b : BaseType.values())
			{
				if(block_type >= b.block_type && block_type < b.block_type + NUM_BASES_PER_TYPE)
				{
					return b;
				}
			}
			return null;//Unknown base type
		}
	}
}