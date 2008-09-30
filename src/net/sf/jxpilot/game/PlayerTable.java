package net.sf.jxpilot.game;

import java.awt.*;
import java.util.*;
import net.sf.jxpilot.graphics.*;

import static net.sf.jxpilot.data.Teams.*;

public class PlayerTable implements Drawable
{
	private final Color TABLE_COLOR = Color.WHITE;
	
	private final int NAME_OFFSET = 0;
	private final int LIFE_OFFSET = NAME_OFFSET + 80;
	private final int SCORE_OFFSET = LIFE_OFFSET + 40;
	
	/**
	 * Location of display.
	 */
	private int x,y;
	/**
	 * Actual players.
	 */
	private Collection<Player> players;
	
	public PlayerTable(int x, int y,Collection<Player> players)
	{
		this.players = players;
		
		this.x=x;
		this.y=y;
	}
	
	public int getX(){return x;}
	public int getY(){return y;}
	
	public void setX(int x){this.x=x;}
	public void setY(int y){this.y=y;}
	
	
	@Override
	public void paintDrawable(Graphics2D g2d)
	{
		int height = g2d.getFontMetrics().getHeight();
		int i =0;
		
		g2d.setColor(TABLE_COLOR);
		
		g2d.drawString("Name", x+NAME_OFFSET, y);
		g2d.drawString("Lives", x+LIFE_OFFSET, y);
		g2d.drawString("Score", x+SCORE_OFFSET, y);
		i++;
		
		for(int team = MIN_TEAM;team<MIN_TEAM+NUM_TEAMS;team++)
		{
			boolean empty = true;
			for(Player p : players)
			{
				if(p.getTeam()==team)
				{
					empty = false;
					break;
				}
			}
			
			if(!empty)
			{
				g2d.drawString("Team " + team, x, y+i*height);
				i++;
				
				for(Player p : players)
				{
					if(p.getTeam()==team)
					{
						renderPlayer(g2d, p, x, y+i*height);
						i++;
					}
				}
			}
		}
	}
	
	private void renderPlayer(Graphics2D g2d, Player p, int x, int y)
	{
		g2d.drawString(p.getNick(), x+NAME_OFFSET, y);
		g2d.drawString(String.valueOf(p.getLife()), x+LIFE_OFFSET, y);
		g2d.drawString(String.valueOf(p.getScore()), x+SCORE_OFFSET, y);
		
	}
	
}
