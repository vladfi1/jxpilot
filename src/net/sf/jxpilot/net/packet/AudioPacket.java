package net.sf.jxpilot.net.packet;

import net.sf.jxpilot.net.ByteBufferWrap;

/**
 * Holds data from an Audio packet.
 * @author Vlad Firoiu
 */
public final class AudioPacket extends XPilotPacketAdaptor {
	private byte type, volume;
	
	public byte getType(){return type;}
	public byte getVolume(){return volume;}
	
	@Override
	public void readPacket(ByteBufferWrap in) throws PacketReadException {
		pkt_type = in.getByte();
		type = in.getByte();
		volume = in.getByte();
	}
	
	@Override
	public String toString() {
		return "Audio Packet\npacket type = " + pkt_type +
				"\ntype = " + type +
				"\nvolume = " + volume;
	}
}
