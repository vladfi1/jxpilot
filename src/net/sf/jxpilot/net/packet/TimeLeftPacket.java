package net.sf.jxpilot.net.packet;

import net.sf.jxpilot.net.ByteBufferWrap;

/**
 * Holds data from a Time Left packet.
 * @author Vlad Firoiu
 */
public final class TimeLeftPacket extends XPilotPacketAdaptor {
	protected int seconds;
	
	public int getSeconds(){return seconds;}
	
	@Override
	public void readPacket(ByteBufferWrap in) throws PacketReadException {
		pkt_type = in.getByte();
		seconds = in.getInt();
	}
	
	@Override
	public String toString() {
		return "Time Left Packet\npacket type = " + pkt_type +
				"\nseconds = " + seconds;
	}
}
