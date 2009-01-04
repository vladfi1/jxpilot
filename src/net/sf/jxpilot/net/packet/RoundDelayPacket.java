package net.sf.jxpilot.net.packet;

import net.sf.jxpilot.net.ByteBufferWrap;

/**
 * Holds data form a Round Delay packet.
 * @author Vlad Firoiu
 */
public final class RoundDelayPacket extends XPilotPacketAdaptor {
	private short count, max;
	
	public short getCount(){return count;}
	public short getMax(){return max;}

	@Override
	public void readPacket(ByteBufferWrap in) throws PacketReadException {
		pkt_type = in.getByte();
		count = in.getShort();
		max = in.getShort();
	}

	@Override
	public String toString() {
		return "Round Delay Packet\npacket type = " + pkt_type +
				"\ncount = " + count +
				"\nmax = " + max;
	}
}
