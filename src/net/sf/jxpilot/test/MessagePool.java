package net.sf.jxpilot.test;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Vector;

/**
 * Haldles in game messages for JXPilot.
 * 
 * @author Taras Kostiak
 * 
 */
public class MessagePool {

    /**
     * Shows timeout between publishing message and removing it.<br>
     * TODO: Should be replaced with settings(transfered from XPilotPanel).
     */
    public static final long MESSAGE_REMOVE_TIMEOUT = 5 * 60 * 1000;

    /**
     * Shows timeout between publishing message(in white color) and graying it.<br>
     * TODO: Should be replaced with settings(transfered from XPilotPanel).
     */
    public static final long MESSAGE_GRAY_TIMEOUT = 30 * 1000;

    /**
     * Messages stored in this pool.
     */
    private List<TimedMessage> messages = null;

    /**
     * Creates new <code>MessagePool</code>.
     */
    public MessagePool() {
        messages = new Vector<TimedMessage>();
    }

    /**
     * Adds message to print on game screen.
     * 
     * @param message
     *            Message, to print.
     */
    public void publishMessage(String message) {
        messages.add(new TimedMessage(message));
    }

    /**
     * This class stands for message with date of it's publish.
     * 
     * @author Taras Kostiak
     * 
     */
    private class TimedMessage {

        /**
         * Stored message.
         */
        private String message = null;

        /**
         * Time of creating this message.
         */
        private long publishTime = -1;

        /**
         * Creates new <code>TimedMessage</code>, with publish time - current
         * system time.
         */
        public TimedMessage(String message) {
            this.message = message;
            publishTime = System.currentTimeMillis();
        }

        /**
         * @see #message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @see #publishTime
         */
        public long getPublishTime() {
            return publishTime;
        }

    }

    /**
     * Renders current messages into given Graphics2D.
     * 
     * @param g2
     */
    public void render(Graphics2D g2) {
        g2.drawString("TEST", 13, 13);
    }

}
