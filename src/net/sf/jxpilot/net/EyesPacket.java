package net.sf.jxpilot.net;

/**
 * Holds data from an eyes packet.
 * @author Vlad Firoiu
 */
public class EyesPacket extends XPilotPacketAdaptor {
	private short id;
	public short getId(){return id;}
	
	@Override
	public void readPacket(ByteBufferWrap in) throws PacketReadException {
		pkt_type = in.getByte();
		id = in.getShort();
	}
	
	@Override
	public String toString() {
		return "Eyes Packet\npacket type = " + pkt_type +
				"\nid = " + id;
	}
}
