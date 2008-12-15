package net.sf.jxpilot.net;

import static net.sf.jxpilot.util.Utilities.removeNullCharacter;
import net.sf.jxpilot.game.ShipShape;

/**
 * Represents data from a Player packet.
 * @author Vlad Firoiu
 */
public class PlayerPacket extends XPilotPacket {
	/**
	 * Default packet read exception to throw.
	 */
	protected final ReliableReadException PLAYER_READ_EXCEPTION = new ReliableReadException();
	
	protected short id;
	protected byte my_team, my_char;
	protected String name, real, host;
	protected ShipShape ship_shape;

	//player data
	public short getId(){return id;}
	public short getTeam(){return my_team;}
	public short getChar(){return my_char;}
	public String getName(){return name;}
	public String getReal(){return real;}
	public String getHost(){return host;}
	public ShipShape getShipShape(){return ship_shape;}
	
	public void setPlayer(short id, byte my_team , byte my_char, String name, String real, String host, ShipShape ship_shape) {
		this.id = id;
		this.my_team = my_team;
		this.my_char = my_char;
		this.name = name;
		this.real = real;
		this.host = host;
		this.ship_shape = ship_shape;
	}
	
	public void setFrom(PlayerPacket other) {
		this.setPlayer(other.id, other.my_team, other.my_char, other.name, other.real, other.host, other.ship_shape);
	}
	
	@Override
	public void readPacket(ByteBufferWrap in) throws ReliableReadException {
		pkt_type = in.getByte();
		id = in.getShort();
		my_team = in.getByte();
		my_char = in.getByte();
		try {
			name = removeNullCharacter(in.getString());
			real = removeNullCharacter(in.getString());
			host = removeNullCharacter(in.getString());
			ship_shape = ShipShape.parseShip(in.getString(), in.getString());
		} catch (StringReadException e) {
			throw PLAYER_READ_EXCEPTION;
		}
	}
	
	@Override
	public String toString() {
		return "Player Packet\npacket type = " + pkt_type +
				"\nid = " + id +
				"\nmy team = " + my_team +
				"\nmy char = " + my_char +
				"\nname = " + name +
				"\nreal = " + real +
				"\nhost = " + host +
				"\nship shape = " + ship_shape;
	}
}
