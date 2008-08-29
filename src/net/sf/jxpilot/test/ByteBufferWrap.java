package net.sf.jxpilot.test;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;

/**
 * Wrapper class that handles buffer flips automatically. It does not actually extend ByteBuffer since
 * ByteBuffer has only private methods/constructors.
 * @author vlad
 */

public class ByteBufferWrap
{
	public static final byte END_OF_STRING = 0x00;
	
	private boolean writing=true;
	private ByteBuffer buffer;
	private int mark=0;
	
	public ByteBufferWrap(int capacity)
	{
		buffer = ByteBuffer.allocate(capacity);
	}
	
	public boolean isWriting(){return writing;}
	public boolean isReading(){return !writing;}
	
	public void setWriting()
	{
		if (!writing)
		{
			mark = buffer.position();
			buffer.position(buffer.limit());
			buffer.limit(buffer.capacity());
			writing = true;
		}
	}
	
	public void setReading()
	{
		if (writing)
		{
			buffer.limit(buffer.position());
			buffer.position(mark);
			writing = false;
		}
	}
	
	public void clear()
	{
		writing = true;
		buffer.clear();
		mark = 0;
	}
	
	/**
	 * This method does not automatically clear the underlying ByteBuffer before reading a UDP packet.
	 * @param channel The UDP channel to read from.
	 * @return The number of bytes read.
	 * @throws IOException if channel.read throws an IOException.
	 * @return The number of bytes read (possibly 0) or -1 if no packets available
	 * 			and the channel is in non-blocking mode.
	 */
	public int readPacket(DatagramChannel channel) throws IOException
	{
		setWriting();
		return channel.read(buffer);
	}
	
	public void sendPacket(DatagramChannel channel, SocketAddress address) throws IOException
	{
		setReading();
		channel.send(buffer, address);
	}
	
	public int remaining()
	{
		if (writing)
		{
			return buffer.position()-mark;
		}
		else
			return buffer.remaining();
	}
	
	public int position()
	{
		return buffer.position();
	}
	
	public void position(int pos)
	{
		buffer.position(pos);
	}
	
	public void compact()
	{
		buffer.compact();
		mark = 0;
		writing = true;
	}
	
	public byte[] getArray()
	{
		return buffer.array();
	}
	
	public ByteBufferWrap putByte(byte b)
	{
		setWriting();
		buffer.put(b);
		return this;
	}
	
	public ByteBufferWrap putBytes(byte[] bytes)
	{
		setWriting();
		buffer.put(bytes);
		return this;
	}
	
	public ByteBufferWrap putBytes(ByteBufferWrap bytes)
	{
		setWriting();
		bytes.setReading();
		buffer.put(bytes.buffer);
		return this;
	}
	
	public ByteBufferWrap putChar(char c)
	{
		setWriting();
		
		buffer.putChar(c);
		return this;
	}
	
	public ByteBufferWrap putShort(short s)
	{
		setWriting();
		
		buffer.putShort(s);
		return this;
	}	

	public ByteBufferWrap putInt(int i)
	{
		setWriting();
		
		buffer.putInt(i);
		return this;
	}
	public ByteBufferWrap putLong(long l)
	{
		setWriting();
		
		buffer.putLong(l);
		return this;
	}
	/**
	 * @param str The String to be put into the buffer using ASCII encoding.
	 * 				A null char is placed at the end if none is included in the String.
	 * @return This ByteBufferWrap.
	 */
	public ByteBufferWrap putString(String str)
	{
		setWriting();
		
		try
		{
			buffer.put(str.getBytes("US-ASCII"));
			if (!str.endsWith(String.valueOf((char)END_OF_STRING)))
			buffer.put(END_OF_STRING);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return this;
	}

	public byte getByte()
	{
		setReading();
		return buffer.get();
	}
	
	/**
	 * @return Same as getByte(), but doesn't increment the buffer's position.
	 */
	public byte peekByte()
	{
		setReading();
		byte b = buffer.get();
		buffer.position(buffer.position()-1);
		return b;
	}
	
	public short getUnsignedByte()
	{
		return Utilities.getUnsignedByte(getByte());
	}
	
	public short peekUnsignedByte()
	{
		return Utilities.getUnsignedByte(peekByte());
	}
	
	public char getChar()
	{
		setReading();
		return buffer.getChar();
	}
	
	public short getShort()
	{
		setReading();
		return buffer.getShort();
	}	
	
	public int getUnsignedShort()
	{
		return Utilities.getUnsignedShort(getShort());
	}
	
	public int getInt()
	{
		setReading();
		return buffer.getInt();
	}	
	
	public long getLong()
	{
		setReading();
		return buffer.getLong();
	}
	
	private StringBuilder strBuf = new StringBuilder();
	
	/**
	 * @return A String read from the buffer using ASCII encoding.
	 * @throws StringReadException If the buffer is read to the end but no null char is found.
	 */
	public String getString() throws StringReadException
	{
		setReading();
		
		StringBuffer b = new StringBuffer();
		
		byte ch;
		//boolean ends_with_null = false;

		do
		{

			if (buffer.remaining()<=0)
			{
				/*
					System.out.println("*****error reading string!******");
					String soFar = "";

					for (byte c : b.toString().getBytes())
					{
						soFar += String.format("%x ", c);
					}
					System.out.println("So far getString() has read:\n" +soFar);
				 */
				throw stringReadException;
			}


			ch = buffer.get();

			//if (ch==END_OF_STRING)
			//	ends_with_null = true;

			b.append((char)ch);

		} while(ch != END_OF_STRING);

		return b.toString();
	}
	
	private static StringReadException stringReadException = new StringReadException();
}