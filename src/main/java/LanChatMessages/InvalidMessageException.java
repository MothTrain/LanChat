package LanChatMessages;

public class InvalidMessageException extends RuntimeException {
    
    public InvalidMessageException() {
        super("The message is not valid.");
    }
    
    public InvalidMessageException(String message) {
        super(message);
    }
}
