package LanChatMessages;

import com.github.cliftonlabs.json_simple.JsonObject;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

/**
 * The message class is used to carry json messages
 * that are received or are to be sent through LanChat.
 * <br>
 * This class stores the and parses the data using a private instance
 * of the {@link com.github.cliftonlabs.json_simple} library
 * <br>
 * Should be fully
 * compliant with the enum constant requirements of the {@link MessageTypes}
 * class. It must validate all messages that it's instances carry on initialisation
 * and throw exceptions when not met. The full connection protocol is specified
 * in the {@link MessageTypes} class
 */
public class Message {

    /**
     * Creates an instance of the Message class
     *
     * @param message the {@link com.github.cliftonlabs.json_simple.JsonObject} that the object will carry
     * @param currentStage the current connection stage of the manager
     * @throws InvalidMessageException if the message was invalid
     */
    public Message(JsonObject message, int currentStage) throws InvalidMessageException {
        if(!isValidMessage(message, currentStage)) {throw new InvalidMessageException("Message contained invalid contents");}
        
        this.stage = currentStage;
        this.message = message;
    }
    
    /**
     * Contains the {@code final} message that the {@link Message} will carry
     */
    private final JsonObject message;
    
    /**
     * {@code Final} connection stage of the at the time of initialisation
     *
     * @see MessageTypes
     */
    private final int stage;
    
    
    /**
     * Checks if the given {@code JsonObject} contains all the
     * required fields and is a message of the correct connection
     * stage.
     * <br>
     * The Json message is final so this is only necessary
     * at initialisation
     *
     * @param test the JSON message to be tested
     * @param currentStage the connection stage
     * @return a boolean indicating whether the message is valid
     */
    static private boolean isValidMessage(JsonObject test, int currentStage) {
        MessageTypes type;
        try {
            type = MessageTypes.valueOf((String) test.get("MsgType"));
            
            
            for (String argument : type.getArguments()) {
                if(test.get(argument) == null) {return false;}
            }
            
            if (type.getStage() == Integer.MAX_VALUE) {return true;}
            if (type.getStage() != currentStage) {return false;}
            
        } catch (ClassCastException e) {
            return false; //Should not be possible
        } catch (IllegalArgumentException e) {
            return false; //Unknown message type or missing "MsgType"
        }
        
        return true;
    }
    
    public String toJson() {
        return message.toJson();
    }
    
    
    @Override
    public String toString() {
        return message.toJson();
    }
    
    /**
     * MessageTypes details the message requirements that the Json messages
     * sent through LanChat must comply with. The documentation over each constant
     * also specifies the purpose of each message and the action taken by the sending
     * and receiving node.
     */
    public enum MessageTypes {
        /**
         * The {@link #NEW_CONNECTION} is a {@link #stage} 1 message, that is sent to a listening
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
        DECLINE_CONNECTION(new String[]{"MsgType", "Username"}, 2),

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
        ACCEPT_CONNECTION(new String[]{"MsgType"}, 2),
        
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
         * The receiving node must reset its WATCHDOG_KICK sender timer. This should be
         * automatic in the form of a timeout {@link ServerSocket#accept()}
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
         * Gets the required arguments for the message type
         * @return the required arguments
         */
        public String[] getArguments() {
            return arguments;
        }
        
        /**
         * Gets the required connection stage of the message type
         * @return the connection stage
         */
        public Integer getStage() {
            return stage;
        }
        
        /**
         * The json field names that the message must contain
         */
        private final String[] arguments;
        
        /**
         * The stage that the connection must be at
         */
        private final int stage;
        
        
    }
}
