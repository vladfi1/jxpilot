package net.sf.jxpilot.util;

/**
 * Class that holds a number of bits that is a multiple of 8.
 * @author vlad
 *
 */
public class BitVector
{
	public static final int BITS_PER_BYTE = 8;
	
	private static final byte[] masks;
	
	static
	{
		masks = new byte[BITS_PER_BYTE];
		
		for (int i =0;i<BITS_PER_BYTE;i++)
		{
			masks[i]=1;
			
			masks[i] <<= i;
		}
	}
	public final int numBits;
	public final int numBytes;
	private final byte[] bytes;
	
	public BitVector(int numBits)
	{
		this.numBits = numBits;
		numBytes = 1+(numBits-1)/BITS_PER_BYTE;
		bytes = new byte[numBytes];
	}
	
	public byte[] getBytes(){return bytes;}
	
	private int byteIndex(int bitIndex)
	{
		return bitIndex / BITS_PER_BYTE;
	}
	
	private int bitIndexInByte(int bitIndex)
	{
		return bitIndex % BITS_PER_BYTE;
	}
	
	public boolean isSet(int bitIndex)
	{
		return (bytes[byteIndex(bitIndex)] & masks[bitIndexInByte(bitIndex)])!=0;
	}
	
	public BitVector setBit(int bitIndex, boolean value)
	{
		if (value)
		{
			bytes[byteIndex(bitIndex)] |= masks[bitIndexInByte(bitIndex)];
		}
		else
		{
			bytes[byteIndex(bitIndex)] &= ~masks[bitIndexInByte(bitIndex)];
		}
		return this;
	}
	
	public BitVector switchBit(int bitIndex)
	{
		bytes[byteIndex(bitIndex)] ^= masks[bitIndexInByte(bitIndex)];
		return this;
	}
	
	public BitVector clearBits()
	{
		for (int i =0;i<bytes.length;i++)
		{
			bytes[i] = 0;
		}
		return this;
	}
	public BitVector setBits()
	{
		for (int i =0;i<bytes.length;i++)
		{
			bytes[i] = ~0x00;
		}
		return this;
	}
	
	public String toString()
	{
		StringBuilder temp = new StringBuilder();
		
		for (int i=0;i<numBytes;i++)
		{
			temp.append(String.format("%x", bytes[i]));
		}
		
		return temp.toString();
	}
}
