package net.sf.jxpilot.test;

/**
 * This exception is thrown when the reliable data stream doesn't have 
 * enough data left for a reliable packet. This is normal, since reliable data comes as
 * stream and xpilot packets may be split up among UDP packets (unfortunately).
 * @author vlad
 */

public class ReliableReadException extends PacketReadException{

}
