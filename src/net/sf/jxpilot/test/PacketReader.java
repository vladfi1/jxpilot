package net.sf.jxpilot.test;

import java.nio.*;

public interface PacketReader
{
	public void readPacket(ByteBuffer buf, AbstractClient client);
}
