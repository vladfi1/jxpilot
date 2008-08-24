package net.sf.jxpilot.test;

import static net.sf.jxpilot.test.Packet.*;
import static net.sf.jxpilot.test.UDPTest.*;
import static net.sf.jxpilot.test.ReliableDataError.*;
import static net.sf.jxpilot.test.Ack.*;

class ReliableData
{
	public static final int LENGTH = 1+2+4+4;
	
	public static ReliableDataError readReliableData(ReliableData data, ByteBufferWrap in, ByteBufferWrap out, NetClient client)
	{
		//buf.rewind();
		
		in.setReading();
		
		if (in.remaining()<LENGTH) return BAD_PACKET;
		data.setData(in.getByte(), in.getShort(),in.getInt() , in.getInt());
		
		if (data.getPktType()!=PKT_RELIABLE)
		{
			in.position(in.position()-1);
			return NOT_RELIABLE_DATA;
		}
		if (data.len > in.remaining()) {
			
			in.clear();
			System.out.println("Not all reliable data in packet");
			//Sockbuf_advance(&rbuf, rbuf.ptr - rbuf.in);
			return BAD_PACKET;
		}
		if (data.getRel() > data.getOffset()) {
			/*
			 * We miss one or more packets.
			 * For now we drop this packet.
			 * We could have kept it until the missing packet(s) arrived.
			 */
			
			in.position(in.position()+data.len);
			//System.out.println("Packet out of order");
			return OUT_OF_ORDER;
		}
		if (data.getRel() + data.getLen() <= data.getOffset()) {
			/*
			 * Duplicate data.  Probably an ack got lost.
			 * Send an ack for our current stream position.
			 */
			
			in.position(in.position()+data.len);
			client.sendAck(out, Ack.ack.setAck(data));
			//System.out.println("Duplicate Data");
			return DUPLICATE_DATA;
		}
		
		
		if (data.rel < offset) {
			data.len -= (short)(offset - data.rel);
			
			in.position(in.position()+offset-data.rel);
			
			data.rel = offset;
		}
		
		
		System.out.println(data);
		
		data.incrementOffset();
		client.sendAck(out, ack.setAck(data));
		
		return NO_ERROR;
	}

	public static ReliableDataError readReliableData(ReliableData data, ByteBufferWrap in, ByteBufferWrap out, NetClient client, ByteBufferWrap reliableBuf)
	{
		ReliableDataError error = readReliableData(data, in, out, client);
		
		if (error == NO_ERROR)
		{
			//reliableBuf.put(in.array(), in.position(), data.getLen());
			//in.position(in.position() + data.getLen());
			
			for(int i =0;i<data.getLen();i++)
			{
				reliableBuf.putByte(in.getByte());
			}
		}
		
		return error;
	}
	
	/*
	public static ReliableDataError getReliableData(ReliableData data, ByteBufferWrap in, ByteBufferWrap out, ByteBufferWrap reliableBuf)
	{
		receivePacket(in);
		in.flip();
		
		ReliableDataError error = readReliableData(data, in, out);
		reliableBuf.put(in);
		
		return error;
	}
	*/
	
	private static int offset=0;

	private byte pkt_type;
	private short len;
	private int rel;
	private int rel_loops;

	public ReliableData setData(byte pkt_type, short len, int rel, int rel_loops)
	{
		this.pkt_type = pkt_type;
		this.len = len;
		this.rel = rel;
		this.rel_loops = rel_loops;
		return this;
	}

	public byte getPktType(){return pkt_type;}
	public short getLen(){return len;}
	public int getRel(){return rel;}
	public int getRelLoops(){return rel_loops;}

	private void incrementOffset()
	{
		offset += len;
	}

	public static int getOffset()
	{
		return offset;
	}

	public String toString()
	{
		return "Reliable Data:\n"
		+"pkt_type = " + String.format("%x", pkt_type)
		+"\nlen = " + len
		+"\nrel = " + rel
		+"\nrel_loops = " + rel_loops;
	}
}