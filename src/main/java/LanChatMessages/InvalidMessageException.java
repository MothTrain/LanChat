package LanChatMessages;

public class InvalidMessageException extends Exception {
    
    public InvalidMessageException() {
        super("The message is not valid.");
    }
    
    public InvalidMessageException(String message) {
        super(message);
    }
}
