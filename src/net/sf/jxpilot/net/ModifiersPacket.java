package net.sf.jxpilot.net;

/**
 * Holds data from a modifiers packet.
 * @author Vlad Firoiu
 */
public final class ModifiersPacket extends XPilotPacketAdaptor {
	private final PacketReadException MODIFIERS_READ_EXCEPTION = new PacketReadException("Modifiers");

	private String modifiers;
	public String getModifiers(){return modifiers;}
	
	@Override
	public void readPacket(ByteBufferWrap in) throws PacketReadException {
		pkt_type = in.getByte();
		try {
			modifiers = in.getString();
		} catch(StringReadException e) {
			throw MODIFIERS_READ_EXCEPTION;
		}
	}

	@Override
	public String toString() {
		return "Modifiers Packet\npacket type = " + pkt_type +
				"\nmodifiers = " + modifiers;
	}
}
