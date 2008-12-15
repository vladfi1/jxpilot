package net.sf.jxpilot.net;

public abstract class XPilotPacket {
	protected byte pkt_type;
	
	public byte getPacketType(){return pkt_type;}
	
	public abstract void readPacket(ByteBufferWrap in) throws PacketReadException;
}
