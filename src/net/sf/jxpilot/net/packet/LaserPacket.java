package net.sf.jxpilot.net.packet;

import net.sf.jxpilot.game.LaserHolder;
import net.sf.jxpilot.net.ByteBufferWrap;

/**
 * Holds data from a Laser packet.
 * @author Vlad Firoiu
 */
public final class LaserPacket extends LaserHolder implements XPilotPacket {

	private byte pkt_type;
	
	@Override
	public byte getPacketType() {return pkt_type;}

	@Override
	public void readPacket(ByteBufferWrap in) throws PacketReadException {
		pkt_type = in.getByte();
		color = in.getByte();
		x = in.getShort();
		y = in.getShort();
		len = in.getShort();
		dir = in.getByte();
	}

	@Override
	public String toString() {
		return "Laser Packet\ntype = " + pkt_type +
				"\ncolor = " + color +
				"\nx = " + x +
				"\ny = " + y +
				"\nlen = " + len +
				"\ndir = " + dir;
	}
}
