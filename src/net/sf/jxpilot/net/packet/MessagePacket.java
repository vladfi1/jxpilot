package net.sf.jxpilot.net.packet;

import static net.sf.jxpilot.util.Utilities.removeNullCharacter;
import net.sf.jxpilot.net.ByteBufferWrap;
import net.sf.jxpilot.net.StringReadException;

/**
 * Holds data from a Message packet.
 * @author Vlad Firoiu
 */
public final class MessagePacket extends XPilotPacketAdaptor {
	private final ReliableReadException MESSAGE_READ_EXCEPTION = new ReliableReadException("Message");
	
	private String message;
	public String getMessage(){return message;}
	
	@Override
	public void readPacket(ByteBufferWrap in) throws ReliableReadException {
		//int pos = in.position();
        try {
            pkt_type = in.getByte();
            message = in.getString();
        } catch (StringReadException e) {
            // e.printStackTrace();
            //in.position(pos);
            throw MESSAGE_READ_EXCEPTION;
        }

        if (message != null) {
            message = removeNullCharacter(message);
        }
	}
	
	@Override
	public String toString() {
		return "Message Packet\npacket type = " + pkt_type +
				"\nmessage = " + message;
	}
}
