package net.sf.jxpilot.net;

import static net.sf.jxpilot.util.Utilities.removeNullCharacter;
import net.sf.jxpilot.game.ShipShape;
import net.sf.jxpilot.game.PlayerHolder;

/**
 * Represents data from a Player packet.
 * @author Vlad Firoiu
 */
public class PlayerPacket extends PlayerHolder implements XPilotPacket {
	/**
	 * Default packet read exception to throw.
	 */
	protected final ReliableReadException PLAYER_READ_EXCEPTION = new ReliableReadException();
	
	protected byte pkt_type;
	
	public byte getPacketType(){return pkt_type;}

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
