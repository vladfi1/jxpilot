package net.sf.jxpilot.net;

import static net.sf.jxpilot.JXPilot.*;
import static net.sf.jxpilot.net.Ack.*;
import static net.sf.jxpilot.test.Packet.*;
import static net.sf.jxpilot.test.ReliableDataError.*;
import net.sf.jxpilot.JXPilot;
import net.sf.jxpilot.test.ByteBufferWrap;
import net.sf.jxpilot.test.ReliableDataError;

/**
 * Class to hold reliable data packets.
 * Note that this class takes complete care of the reliable data stream, including
 * dropping duplicate/out of order packets, copying reliable data, and sending acks(through a client).
 * 
 * @author vlad
 *
 */
class ReliableData
{
	public static final int LENGTH = 1+2+4+4;
	
	public static ReliableDataError readReliableData(ReliableData data, ByteBufferWrap in, NetClient client)
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
			client.sendAck(Ack.ack.setAck(data));
			//System.out.println("Duplicate Data");
			return DUPLICATE_DATA;
		}
		
		if (data.rel < offset) {
			data.len -= (short)(offset - data.rel);
			
			in.position(in.position()+offset-data.rel);
			
			data.rel = offset;
		}
		
		
		if (JXPilot.PRINT_RELIABLE)
			System.out.println(data);
		
		data.incrementOffset();
		client.sendAck(ack.setAck(data));
		
		return NO_ERROR;
	}
	/**
	 * 
	 * @param data The data to read into.
	 * @param in The ByteBufferWrap to read from.
	 * @param client The client to send ack to.
	 * @param reliableBuf The ByteBufferWrap to copy the reliable data into.
	 * @return An appropriate ReliableDataError.
	 */
	public static ReliableDataError readReliableData(ReliableData data, ByteBufferWrap in, NetClient client, ByteBufferWrap reliableBuf)
	{
		ReliableDataError error = readReliableData(data, in, client);
		
		if (error == NO_ERROR)
		{
			//reliableBuf.put(in.array(), in.position(), data.getLen());
			//in.position(in.position() + data.getLen());
			
			/*
			for(int i =0;i<data.getLen();i++)
			{
				reliableBuf.putByte(in.getByte());
			}
			*/
			
			reliableBuf.putBytes(in);
		}
		
		return error;
	}
	
	public ReliableDataError readReliableData(ByteBufferWrap in, NetClient client)
	{
		return readReliableData(this, in, client);
	}
	
	public ReliableDataError readReliableData(ByteBufferWrap in, NetClient client, ByteBufferWrap reliableBuf)
	{
		return readReliableData(this, in, client, reliableBuf);
	}
	
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
		return "\nReliable Data:\n"
		+"pkt_type = " + String.format("%x", pkt_type)
		+"\nlen = " + len
		+"\nrel = " + rel
		+"\nrel_loops = " + rel_loops;
	}
}