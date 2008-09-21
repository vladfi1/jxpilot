package net.sf.jxpilot.game;

import net.sf.jxpilot.map.*;
import net.sf.jxpilot.graphics.Drawable;
import net.sf.jxpilot.util.*;
import java.awt.*;
import java.awt.geom.*;


/**
 * Class that holds/draws the Heads Up Display.
 * @author Vlad
 *
 */
public class HUD {
	
	private GameWorld world;
	private BlockMap map;
	private BlockMapSetup setup;
	
	private final int RADARS_SIZE = 10;
	private HolderList<RadarHolder, Radar> radarHandler;
	
	public HUD(GameWorld world)
	{
		this.world = world;
		map = world.getMap();
		setup = map.getSetup();
		
		RADAR_WIDTH = 256;
		RADAR_HEIGHT = (RADAR_WIDTH * setup.getY()) / setup.getX();
		
		HUD_RADAR_WIDTH  = HUD_RADAR_SCALE * RADAR_WIDTH;
		HUD_RADAR_HEIGHT = HUD_RADAR_SCALE * RADAR_HEIGHT;
		
		X_FACTOR = (float) HUD_RADAR_WIDTH / map.getWidth();
		Y_FACTOR = (float) HUD_RADAR_HEIGHT / map.getHeight();
		
		//System.out.println("X Factor = " + X_FACTOR + "\nY Factor = " + Y_FACTOR);
		
		initRadars();
	}
	
	private void initRadars()
	{
		radarHandler = new HolderList<RadarHolder, Radar>(radarFactory, RADARS_SIZE);
	}
	
	public Iterable<Radar> getRadarHandler(){return radarHandler;}
	
	
	public void addRadar(RadarHolder r)
	{
		radarHandler.add(r);
	}
	
	public void update()
	{
		radarHandler.clear();
	}
	
	//"static" radar data
	private final int RADAR_WIDTH, RADAR_HEIGHT;
    
	private final int HUD_RADAR_SCALE = 1;
	private final int HUD_RADAR_WIDTH, HUD_RADAR_HEIGHT;
	
	private final float X_FACTOR, Y_FACTOR;
	
	public class Radar extends RadarHolder implements Drawable
	{
		
		/*
	    int		i;
	    int		hrscale = 3;
	    int		hrw = hrscale * 256;
	    int		hrh = hrscale * RadarHeight;
	    float	xf = (float)hrw / (float)Setup->width, 
			yf = (float)hrh / (float)Setup->height;

	    for (i = 0; i < num_radar; i++) {

	        int sz = radar_ptr[i].size;

		// skip non-enemy objects 
	        if ((sz & 0x80) == 0) {

	            int x = radar_ptr[i].x * hrscale - 
	                (world.x + ext_view_width / 2) * xf;

	            int y = radar_ptr[i].y * hrscale - 
	                (world.y + ext_view_height / 2) * yf;
            if (BIT(Setup->mode, WRAP_PLAY)) {
                if (x < 0) {
                    if (-x > hrw/2) x += hrw;
                } else {
                    if (x > hrw/2) x -= hrw;
                }

                if (y < 0) {
                    if (-y > hrh/2) y += hrh;
                } else {
                    if (y > hrh/2) y -= hrh;
                }
            }

            sz = (sz > 0) ? sz * hrscale : hrscale;

            Arc_add(hudColor,
                    x + ext_view_width / 2 - sz / 2, 
                    -y + ext_view_height / 2 - sz / 2, 
                    sz, sz, 0, 64*360);
 
      
		 */
		
		public final Color RADAR_COLOR = Color.RED;
		private final Ellipse2D radarShape = new Ellipse2D.Float();
		
		public void paintDrawable(Graphics2D g2d)
		{
			//if((super.size & 0x80) != 0) return;
			
			int radarX = super.x * HUD_RADAR_SCALE - (int)((world.getSelfX()) * X_FACTOR);
			int radarY = super.y * HUD_RADAR_SCALE - (int)((world.getSelfY()) * Y_FACTOR);
			
			if(setup.wrapPlay())
			{
				if (radarX < 0) {
                    if (-radarX > HUD_RADAR_WIDTH/2) radarX += HUD_RADAR_WIDTH;
                } else {
                    if (radarX > HUD_RADAR_WIDTH/2) radarX -= HUD_RADAR_WIDTH;
                }

                if (radarY < 0) {
                    if (-radarY > HUD_RADAR_HEIGHT/2) radarY += HUD_RADAR_HEIGHT;
                } else {
                    if (radarY > HUD_RADAR_HEIGHT/2) radarY -= HUD_RADAR_HEIGHT;
                }
			}
			
			//skips if radar would be painted on ship
			
			if(radarX*radarX + radarY*radarY < GameWorld.Ship.SHIP_RADIUS*GameWorld.Ship.SHIP_RADIUS){return;}
			
			g2d.setColor(RADAR_COLOR);
			
			radarShape.setFrame((radarX+world.getSelfX()-super.size),
								(radarY+world.getSelfY()-super.size),
					2*super.size, 2*super.size);
			
			g2d.fill(radarShape);
			
			/*
			System.out.println("Radar drawn:" +
								"\nx = " + super.x + 
								"\ny = " + super.y +
								"\nRadar X = " + radarX +
								"\nRadar Y = " + radarY +
								"\nsize = " + size);
			*/
		}
	}
	public final Factory<Radar> radarFactory = new Factory<Radar>()
	{
		public Radar newInstance(){return new Radar();}
	};
	
}