package net.sf.jxpilot.test;

public class ReplyMessage
{
	private int magic;
	private byte pack_type, status;
	private int value;
	
	public ReplyMessage setMessage(int magic, byte pack_type, byte status, short value)
	{
		this.magic = magic;
		this.pack_type = pack_type;
		this.status = status;
		this.value = getUnsignedShort(value);
		return this;
	}
	
	public int getMagic(){return magic;}
	public byte getPack(){return pack_type;}
	public byte getStatus(){return status;}
	public int getValue(){return value;}
	
	public String toString()
	{
		return "Reply Message:\n"
				+"\nmagic = " + String.format("%x", magic)
				+"\npack_type = " + String.format("%x", pack_type)
				+"\nstatus = " + String.format("%x", status)
				+"\nvalue = " + String.format("%x", value);
	}
	
	public static int getUnsignedShort(short val)
	{
		return (int)((char)val);
	}
	
	public static ReplyMessage readReplyMessage(ByteBufferWrap buf, ReplyMessage message)
	{
		message.setMessage(buf.getInt(), buf.getByte(), buf.getByte(), buf.remaining()>=2 ? buf.getShort(): 0);
		return message;
	}
	public static ReplyMessage getReplyMessage(ByteBufferWrap buf, ReplyMessage message)
	{
		UDPTest.receivePacket(buf);
		//buf.flip();
		return readReplyMessage(buf, message);
	}

}
