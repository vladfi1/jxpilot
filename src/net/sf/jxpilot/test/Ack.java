package net.sf.jxpilot.test;

import static net.sf.jxpilot.test.Packet.PKT_ACK;

class Ack
{
	public static Ack ack = new Ack();
	
	private byte type = PKT_ACK;
	private int reliable_offset;
	private int reliable_loops;
	
	public Ack setAck(int offset, int loops)
	{
		reliable_offset = offset;
		reliable_loops = loops;
		return this;
	}
	
	public Ack setAck(ReliableData data)
	{
		return setAck(data.getOffset(), data.getRelLoops());
	}
	
	
	public static void putAck(ByteBufferWrap buf, Ack ack)
	{
		//buf.clear();
	
		buf.putByte(ack.getType());
		buf.putInt(ack.getOffset());
		buf.putInt(ack.getLoops());
	}
	
	public byte getType(){return type;}
	public int getOffset(){return reliable_offset;}
	public int getLoops(){return reliable_loops;}
	
	public String toString()
	{
		return  "Acknowledgement"
				+ "\ntype = " + String.format("%x", type)
				+ "\nreliable offset = " + reliable_offset
				+ "\nreliable loops = " + reliable_loops;
	}
}
