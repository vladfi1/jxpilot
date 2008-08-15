package net.sf.jxpilot.test;

import java.nio.ByteBuffer;
import static net.sf.jxpilot.test.Packet.*;
import static net.sf.jxpilot.test.UDPTest.*;
import static net.sf.jxpilot.test.ReliableDataError.*;
import static net.sf.jxpilot.test.Ack.*;

class ReliableData
{
	public static final int LENGTH = 1+2+4+4;
	
	public static ReliableDataError readReliableData(ReliableData data, ByteBuffer in, ByteBuffer out)
	{
		//buf.rewind();
		
		if (in.remaining()<LENGTH) return BAD_PACKET;
		
		data.setData(in.get(), in.getShort(),in.getInt() , in.getInt());
		
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
			sendAck(out, Ack.ack.setAck(data));
			//System.out.println("Duplicate Data");
			return DUPLICATE_DATA;
		}
		
		//System.out.println(data);
		
		data.incrementOffset();
		sendAck(out, ack.setAck(data));

		return NO_ERROR;
	}

	/*
	public static ReliableDataError getReliableData(ReliableData data, ByteBuffer in, ByteBuffer reliableBuf)
	{
		receivePacket(in);
		in.flip();
		
		ReliableDataError error = readReliableData(data, in);
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