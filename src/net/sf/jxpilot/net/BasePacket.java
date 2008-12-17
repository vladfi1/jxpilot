package net.sf.jxpilot.net;

import net.sf.jxpilot.game.BaseHolder;

/**
 * Holds data from a Base packet.
 * @author Vlad Firoiu
 */
public final class BasePacket extends BaseHolder implements XPilotPacket {
	/**
	 * Length of base packet (7).
	 */
	public static final int LENGTH = 1 + 2 + 4;//7
	
	private final ReliableReadException BASE_READ_EXCEPTION = new ReliableReadException();
	
	private byte pkt_type;
	
	public byte getPacketType(){return pkt_type;}
	
	@Override
	public void readPacket(ByteBufferWrap in) throws ReliableReadException {
		if(in.remaining()<LENGTH) throw BASE_READ_EXCEPTION;
		pkt_type = in.getByte();
		id = in.getShort();
		num = in.getShort();
	}
	
	@Override
	public String toString() {
		return "Base Packet\npacket type = " + pkt_type +
				"\nid = " + id +
				"\nnum = " + num;
	}
}
