package LanChatMessages;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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
public final class Message {

    /**
     * Creates a message object using a json object
     *
     * @param message the {@link com.github.cliftonlabs.json_simple.JsonObject} that the object will carry
     * @param currentStage the current connection stage of the manager
     * @throws InvalidMessageException if the message was invalid
     */
    public Message(JsonObject message, int currentStage) throws InvalidMessageException {
        if(!isValidMessage(message, currentStage)) {
            throw new InvalidMessageException("Message contained invalid contents");
        }
        
        this.message = message;
    }
    
    /**
     * Creates a message object using a JSON string
     *
     * @param message the JSON string to deserialize
     * @param currentStage the current connection stage of the manager
     * @throws InvalidMessageException if the JSON contained invalid contents or could not be deserialized
     */
    
    public Message(String message, int currentStage) throws InvalidMessageException {
        
        try {
            this.message = (JsonObject) Jsoner.deserialize(message);
        } catch (JsonException e) {
            throw new InvalidMessageException(e.getMessage());
        }
        
        if(!isValidMessage(this.message, currentStage)) {
            throw new InvalidMessageException("Message contained invalid contents");
        }
        
    }
    
    /**
     * Contains the {@code final} message that the {@link Message} will carry
     */
    private final JsonObject message;
    
    
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
            
            
            for (String argument : type.arguments) {
                if(test.get(argument) == null) {return false;}
            }
            
            if (type.stage == Integer.MAX_VALUE) {return true;}
            if (type.stage != currentStage) {return false;}
            
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
    
    public byte[] toSendableBytes() {
        byte[] json = toJson().getBytes(StandardCharsets.UTF_8);
        byte[] send;
        
        send = ByteBuffer.allocate(json.length+4).putInt(json.length).array();
        System.arraycopy(json, 0, send, 4, json.length);
        
        return send;
    }
    
    @Override
    public String toString() {
        return message.toJson();
    }
    
}
