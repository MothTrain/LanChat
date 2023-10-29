import LanChatMessages.Message;
import LanChatMessages.MessageTypes;

/**
 * The Managerable interface contains callback methods that LanChat managers
 * must implement for senders and listeners to callback to
 */
public interface Managerable {
    
    /**
     * Listeners will call this on the manager when any message is received for the
     * manager to process
     *
     * @param message the message for the manager to process
     * @implSpec Implementors must process the message relevantly to the {@code Action Taken} section
     * of the {@link MessageTypes} constant's documentation
     */
    void messageReceived(Message message);
    
    /**
     * Returns the current connection {@link MessageTypes#stage stage}
     * @return the stage
     */
    int getConnectionStage();
    
    /**
     * Used by the listener and sender threads to report any exceptions encountered
     * in the thread to the manager.
     * @implSpec The following action should be taken if an exception of this type is received:
     *<ul>
     *     <li>{@link java.io.IOException IOException} - The manager should consider the Sender
     *     <B>and</B> Listener dead. The manager should send an {@link MessageTypes#ERROR ERROR} message
     *     using the sender (ignoring any exceptions) and should call their {@code  close()} methods</li>
     * </ul>
     * @implNote This method will only be called by listener and senders
     * if it is sure that the exception originated from the thread and not
     * a method call from an external thread (i.e: the manager). When a method
     * could be called by an external thread or the local thread (i.e: the sender or listener).
     * It will propagate up until it reaches the external thread or the local thread
     * @param e The exception
     */
    void exceptionEncountered(Exception e);
}
