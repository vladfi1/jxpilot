package net.sf.jxpilot.test;


class ReplyData
{
	public static final int LENGTH = 1 + 1 + 1;//3
	
	public static ReplyData readReplyData(ByteBufferWrap buf, ReplyData data)
	{
		//buf.rewind();
		
		data.setData(buf.getByte(), buf.getByte(), buf.getByte());
		return data;
	}
	
	/*
	public static ReplyData getReplyData(ReplyData data, ByteBufferWrap buf)
	{
		
		receivePacket(buf);
		buf.flip();
		return readReplyData(data, buf);
	}
	*/
	
	private byte pkt_type,
					reply_to,
					status;
	
	public ReplyData setData(byte pkt_type, byte reply_to, byte status)
	{
		this.pkt_type = pkt_type;
		this.reply_to = reply_to;
		this.status = status;
		return this;
	}
	
	public byte getPktType(){return pkt_type;}
	public byte getReplyTo(){return reply_to;}
	public byte getStatus(){return status;}
	
	public String toString()
	{
		return "Reply Data:\n"
				+"\npacket type = " + String.format("%x", pkt_type)
				+"\nreply to = " + String.format("%x", reply_to)
				+"\nstatus = " + String.format("%x", status);
	}
	
}