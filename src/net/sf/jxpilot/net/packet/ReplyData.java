package net.sf.jxpilot.net.packet;

import net.sf.jxpilot.net.ByteBufferWrap;

public class ReplyData extends XPilotPacketAdaptor {
	public static final int LENGTH = 1 + 1 + 1;//3
	
	protected final ReliableReadException REPLY_DATA_EXCEPTION = new ReliableReadException();
	
	public static ReplyData readReplyData(ByteBufferWrap in, ReplyData data) throws ReliableReadException {	
		data.readPacket(in);
		return data;
	}
	
	protected byte reply_to, status;
	
	public ReplyData setData(byte pkt_type, byte reply_to, byte status) {
		super.pkt_type = pkt_type;
		this.reply_to = reply_to;
		this.status = status;
		return this;
	}
	
	public byte getReplyTo(){return reply_to;}
	public byte getStatus(){return status;}
	
	@Override
	public String toString() {
		return "Reply Data\npacket type = " + String.format("%x", pkt_type)
				+"\nreply to = " + String.format("%x", reply_to)
				+"\nstatus = " + String.format("%x", status);
	}

	@Override
	public void readPacket(ByteBufferWrap in) throws ReliableReadException {
		if(in.remaining()<LENGTH) throw REPLY_DATA_EXCEPTION;
		setData(in.getByte(), in.getByte(), in.getByte());
	}
	
}