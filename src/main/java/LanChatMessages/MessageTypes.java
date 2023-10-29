package LanChatMessages;

import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

/**
 * MessageTypes details the message requirements that the Json messages
 * sent through LanChat must comply with. The documentation over each constant
 * also specifies the purpose of each message and the action taken by the sending
 * and receiving node. <br>
 * <br>
 * All messages are send in byte form and are headed with the length of the rest
 * of the message
 */
public enum MessageTypes {
    /**
     * The NEW_CONNECTION is a {@link #stage} 1 message, that is sent to a listening
     * socket on {@link #stage} 1, to request a new chat connection be made. <br>
     * <br>
     * Params: <br>
     * <i>IP -</i> the IP address of the sending node<br>
     * <i>Username -</i> the username of the sending node <br>
     * <br>
     * Action taken: <br>
     * The sending node must begin listening for a {@link #DECLINE_CONNECTION}
     * or {@link #ACCEPT_CONNECTION} response from the receiving node on connection
     * {@link #stage} 2. <br>
     * The receiving node must send a {@link #ACCEPT_CONNECTION} or {@link #DECLINE_CONNECTION}
     * through the same port as it is listening on and through the IP it received and
     * listen for {@link #stage} 3 messages
     */
    NEW_CONNECTION(new String[]{"MsgType", "IP", "Username"}, 1),
    
    /**
     * The {@code DECLINE_CONNECTION} is a {@link #stage} 2 message, that is sent to a listening
     * socket on {@link #stage} 2, to decline a {@link #NEW_CONNECTION} message. <br>
     * <br>
     * Params: <br>
     * No additional params
     * <br>
     * Action taken: <br>
     * The sending node must release its listener and sender and show the new connection menu <br>
     * The receiving node must release its listener and sender and show the new connection menu
     */
    DECLINE_CONNECTION(new String[]{"MsgType"}, 2),
    
    /**
     * The {@code ACCEPT_CONNECTION} is a {@link #stage} 2 message, that is sent to a listening
     * socket on {@link #stage} 2, to accept a {@link #NEW_CONNECTION} message. <br>
     * <br>
     * Params: <br>
     * <i>Username -</i> the username of the sending node <br>
     * <br>
     * Action taken: <br>
     * The sending node must begin listening for {@link #stage} 3 messages
     * and display the chat window<br>
     * The receiving node must begin listening for {@link #stage} 3 messages
     * and display and chat window.
     */
    ACCEPT_CONNECTION(new String[]{"MsgType", "Username"}, 2),
    
    /**
     * The {@code MSG} is a {@link #stage} 3 message, that is sent to a listening
     * socket on {@link #stage} 3, to display a sent message/text. <br>
     * <br>
     * Params: <br>
     * <i>Content -</i> the content of the message <br>
     * <br>
     * Action taken: <br>
     * The sending node must display the message itself and continue
     * listening on stage 3<br>
     * The receiving node must display the message and continue
     * listening on stage 3
     */
    MSG(new String[]{"MsgType", "Content"}, 3),
    
    /**
     * The {@code WATCHDOG_KICK} is a {@link #stage} 3 message, that is sent to a listening
     * socket on {@link #stage} 3, to reset receiving node's watchdog. <br>
     * <br>
     * Params: <br>
     * No additional params
     * <br>
     * Action taken: <br>
     * The sending node must reset its WATCHDOG_KICK sender timer. This should be
     * automatic in the form of a timeout
     * {@link java.util.concurrent.LinkedBlockingQueue#poll(long, TimeUnit)}<br>
     * The receiving node must reset its WATCHDOG_KICK sender timer.
     */
    WATCHDOG_KICK(new String[]{"MsgType"}, 3),
    
    /**
     * The {@code WATCHDOG_BARK} is a {@link #stage} 3 message, that is sent to a listening
     * socket on {@link #stage} 3, to check if receiver is online, because of a {@link #WATCHDOG_KICK}
     * timeout. <br>
     * <br>
     * Params: <br>
     * No additional params
     * <br>
     * Action taken: <br>
     * The sending node must wait a reduced timeout. If {@link #WATCHDOG_KICK} or any other
     * valid message is received, the sender continues listening on {@link #stage} 3.
     * In case of timeout, an {@link #ERROR} must be sent, the listener and
     * sender must release their resources and the new connection menu must be displayed <br>
     * The receiving node must immediately send a {@link #WATCHDOG_KICK}.
     */
    WATCHDOG_BARK(new String[]{"MsgType"}, 3),
    
    /**
     * The {@code END_CONNECTION} is a {@link #stage} 3 message, that is sent to a listening
     * socket on {@link #stage} 3, to close a connection. <br>
     * <br>
     * Params: <br>
     * No additional params
     * <br>
     * Action taken: <br>
     * The sending node must release its sender and listener and display the new
     * connection menu<br>
     * The receiving node must release its sender and listener and display the new
     * connection menu
     */
    END_CONNECTION(new String[]{"MsgType"}, 3),
    
    /**
     * The {@code END_CONNECTION} is an any stage message, that is sent to a listening
     * socket on any {@link #stage}, to indicate that the message sender has encountered
     * an irrecoverable connection error and to close the connection <br>
     * <br>
     * Params: <br>
     * <i>Message -</i> The message to display to the user
     * <br>
     * Action taken: <br>
     * The sending node must release its sender and listener and display the new
     * connection menu<br>
     * The receiving node must release its sender and listener and display the new
     * connection menu
     */
    ERROR(new String[]{"MsgType", "Message"}, Integer.MAX_VALUE);
    
    
    MessageTypes(String[] parameters, Integer stage) {
        this.arguments = parameters;
        this.stage = stage;
    }
    
    /**
     * The json field names that the message must contain
     */
    final String[] arguments;
    
    /**
     * The connection stage is a integer from 1-3 of a message type,
     * which the program expects. Eg: if the connection is in stage 3,
     * the {@link Message} class will only accept stage 3 messages.
     * <li>Stage 1: Waiting for a {@link #NEW_CONNECTION} request</li>
     * <li>Stage 2: Waiting for {@link #NEW_CONNECTION} to be accepted or declined</li>
     * <li>Stage 3: Connection has been established</li>
     * <li>Stage {@link Integer#MAX_VALUE}: Allowed at any stage </li>
     */
    final int stage;
}
