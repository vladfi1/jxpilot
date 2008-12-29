package net.sf.jxpilot.net.packet;

import net.sf.jxpilot.net.ByteBufferWrap;

/**
 * Holds data from a shutdown packet.
 * @author Vlad Firoiu
 */
public class ShutdownPacket extends XPilotPacketAdaptor {

	protected short count, delay;
	
	public byte getPacketType(){return pkt_type;}
	public short getCount(){return count;}
	public short getDelay(){return delay;}
	
	@Override
	public void readPacket(ByteBufferWrap in) {
		pkt_type = in.getByte();
		count = in.getShort();
		delay = in.getShort();
	}
	
	@Override
	public String toString() {
		return "Shutdown Packet\npacket type = " + pkt_type +
				"\ncount = " + count +
				"\ndelay = " + delay;
	}
}
