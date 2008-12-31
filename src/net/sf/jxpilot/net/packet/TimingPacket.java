package net.sf.jxpilot.net.packet;

import net.sf.jxpilot.net.ByteBufferWrap;

/**
 * Holds data from a Timing packet.
 * @author Vlad Firoiu
 */
public final class TimingPacket extends XPilotPacketAdaptor {
	/**
	 * The number of bytes in a Timing packet.
	 */
	public static final int LENGTH = 1+2+2;//5
	
	private final ReliableReadException TIMING_READ_EXCEPTION = new ReliableReadException("Timing");
	
	private short id;
	/**
	 * Unsigned short.
	 */
	private short timing;
	
	public short getId(){return id;}
	public short getTiming(){return timing;}
	
	@Override
	public void readPacket(ByteBufferWrap in) throws ReliableReadException {
		if(in.remaining()<LENGTH) throw TIMING_READ_EXCEPTION;
		pkt_type = in.getByte();
		id = in.getShort();
		timing = in.getShort();
	}

	@Override
	public String toString() {
		return "Timing Packet\npacket type = " + pkt_type +
				"\nid = " + id +
				"\ntiming = " + timing;
	}
}
