package net.sf.jxpilot.net;

/**
 * Holds data from a Debris packet.
 * @author Vlad Firoiu
 */
public final class DebrisPacket implements XPilotPacket {
	/**
	 * Unsigned byte.
	 */
	private short pkt_type, num;
	
	@Override
	public byte getPacketType() {return (byte)pkt_type;}
	public short getType(){return pkt_type;}
	public short getNum(){return num;}
	
	@Override
	public void readPacket(ByteBufferWrap in) throws PacketReadException {
		pkt_type = in.getUnsignedByte();
		num = in.getUnsignedByte();
	}
	
	@Override
	public String toString() {
		return "Debris Packet\npacket type = " + pkt_type +
				"\nnum = " + num;
	}
}
