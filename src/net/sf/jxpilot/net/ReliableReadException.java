package net.sf.jxpilot.net;

/**
 * This exception is thrown when the reliable data stream doesn't have 
 * enough data left for a reliable packet. This is normal, since reliable data comes as
 * stream and xpilot packets may be split up among UDP packets (unfortunately).
 * @author Vlad Firoiu
 */
public class ReliableReadException extends PacketReadException {
	private static final long serialVersionUID = 1L;
}
