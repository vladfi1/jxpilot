package net.sf.jxpilot.net;

/**
 * Holds data from a Quit packet.
 * @author Vlad Firoiu
 */
public class QuitPacket extends XPilotPacket {
	protected final ReliableReadException QUIT_READ_EXCEPTION = new ReliableReadException();
	
	protected String reason;
	
	public String getReason(){return reason;}
	
	@Override
	public void readPacket(ByteBufferWrap in) throws ReliableReadException {
		pkt_type = in.getByte();
		try {
			reason = in.getString();
		} catch (StringReadException e) {
			throw QUIT_READ_EXCEPTION;
		}
	}
	
	@Override
	public String toString() {
		return "Quit Packet\npacket type = " + pkt_type +
				"reason = " + reason;
	}

}
