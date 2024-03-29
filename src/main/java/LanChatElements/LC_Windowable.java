package LanChatElements;


/**
 * The LC_Windowable interface provides methods to callback to the manager
 * on the user's actions that are relevant to the manager's operations
 */
public interface LC_Windowable {
    /**
     * Tells the manager to listen on a port chosen by the manager
     *
     * @implSpec The manager must chose a port to listen on and create
     * a listener based on that, to wait for a connection
     * @return The port being listened on
     */
    ConnectionKey connectionListen();
    
    /**
     * Tells the manager to create a connection with the provided key
     * @param connectionKey The connection key with connection information
     *
     * @see ConnectionKey
     */
    void connectTo(String connectionKey, String username);
    
    /**
     * Cancel listening for a new connection (only in stage 1)
     * @implSpec The manager should interrupt the listener only if the stage 1
     */
    void cancelListen();
    
    /**
     * Sends a message through the sender
     *
     * @throws IllegalStateException If the previous message hasn't yet sent
     */
    void sendMessage(String message) throws IllegalStateException;
    
    /**
     * Signals that the user has decided to end the connection
     *
     * @implSpec The manager must send an
     * {@link LanChatMessages.MessageTypes#END_CONNECTION END_CONNECTION}
     * and close the listener and sender
     */
    void endConnection();
    
    /**
     * Signals whether the user has accepted the connection
     *
     * @param allowed True if the user has accepted, false if the user declined
     */
    void allowConnection(boolean allowed, String username);
    
}

