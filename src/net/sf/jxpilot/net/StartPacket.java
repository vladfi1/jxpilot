package net.sf.jxpilot.net;

/**
 * Holds data from a Start packet.
 * @author Vlad Firoiu
 */
public final class StartPacket extends XPilotPacketAdaptor {
	/**
	 * Length of start packet (9).
	 */
	public static final int LENGTH = 1 + 4 + 4;

	private final ReliableReadException START_READ_EXCEPTION = new ReliableReadException();
	
	private int loops, key_ack;
	
	public int getLoops() {return loops;}
	public int getKeyAck(){return key_ack;}
	
	@Override
	public void readPacket(ByteBufferWrap in) throws ReliableReadException {
		if(in.remaining() < LENGTH) throw START_READ_EXCEPTION;
		pkt_type = in.getByte();
		loops = in.getInt();
		key_ack = in.getInt();
	}
	
	@Override
	public String toString() {
		return "Start Packet\npacket type = " + pkt_type +
				"\nloops = " + loops +
				"\nkey ack = " + key_ack;
	}
}
