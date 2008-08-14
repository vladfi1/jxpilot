package net.sf.jxpilot.test;

import java.nio.ByteBuffer;

class ReplyData
{
	
	public static ReplyData readReplyData(ByteBuffer buf, ReplyData data)
	{
		//buf.rewind();
		
		data.setData(buf.get(), buf.get(), buf.get());
		return data;
	}
	
	/*
	public static ReplyData getReplyData(ReplyData data, ByteBuffer buf)
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