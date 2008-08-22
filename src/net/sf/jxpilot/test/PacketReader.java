package net.sf.jxpilot.test;

import java.nio.*;

public interface PacketReader
{
	public void readPacket(ByteBufferWrap buf, AbstractClient client) throws PacketReadException;
}
