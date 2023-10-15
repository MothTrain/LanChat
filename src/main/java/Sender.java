import java.io.*;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import LanChatMessages.Message;
import com.github.cliftonlabs.json_simple.JsonObject;

/**
 * The Sender class is responsible for sending {@link Message Messages} through
 * a socket and kicks the watchdog regularly. Compliant with
 * {@link LanChatMessages.Message.MessageTypes MessageTypes} communication protocol. <br>
 * The socket of Sender is final and if the Sender is closed then a new sender must
 * be created as it cannot be reopened.<br>
 * All exceptions are transmitted through the call back
 */
public class Sender implements AutoCloseable {
    
    /**
     * The socket which the sender will use
     */
    private final Socket socket;
    
    /**
     * The output stream that the socket will be wrapped in for sending messages
     */
    private final DataOutputStream outputStream;
    
    /**
     * An instance of the manager that the sender will use to report back
     */
    private final Managerable callback;
    
    /**
     * The number of milliseconds that the Sender will wait after sending a message
     * that a {@link Message.MessageTypes#WATCHDOG_KICK WATCHDOG_KICK}
     */
    private final long kickRate;
    
    /**
     * The active boolean indicates if the Sender's {@link #thread} method is working.
     * If active is false but the sender is not closed then the message must be written
     * straight to the socket.
     *
     * @see #closed
     */
    private boolean active = false;
    
    /**
     * The closed boolean indicates if the socket is not open and ready to send messages.
     * An exception must be thrown if connected if true, when a message attempted to be sent.
     */
    private boolean closed = true;
    
    /**
     * Queue to write messages that are to be sent. The queue is consumed
     * by the {@link #thread} method. Maximum capacity is 1
     */
    private final LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>(1);
    
    
    /**
     * Creates a sender instance. This does not start the {@link #thread} so
     * {@link Message.MessageTypes#WATCHDOG_KICK WATCHDOG_KICKing} will not happen
     *
     * @param ipAddress the IP address of the host
     * @param port      the port number of the host
     * @param callback  the callback for exception reporting
     * @param kickRate  how often {@link Message.MessageTypes#WATCHDOG_KICK WATCHDOG_KICKs}
     *                  will be sent
     * @throws IOException if an IOException occurs
     */
    public Sender(String ipAddress, int port, Managerable callback, long kickRate) throws IOException {
        
        this.callback = callback;
        this.kickRate = kickRate;
        
        socket = new Socket(ipAddress, port);
        outputStream = new DataOutputStream(socket.getOutputStream());
        
        closed = false;
    }
    
    /**
     * Sends a message through the assigned connection
     *
     * @param message The message to send
     * @throws IllegalStateException If the sender queue is full
     * or the Sender is closed
     * @throws IOException If an IO exception occurs
     *
     * @see #writeThroughSocket(Message)
     * @see #closed
     */
    public synchronized void sendMessage(Message message) throws IllegalStateException, IOException {
        if (closed) {
            throw new IllegalStateException("The Sender is closed");
        } else if (!active) {
            writeThroughSocket(message);
        } else {
            messageQueue.add(message);
        }
    }
    
    /**
     * Writes the message to the output stream. Using the {@link Message#toSendableBytes() toSendableBytes}
     *
     * @param message The Json to send
     * @throws IOException If an IOException occurs during {@link OutputStream} writing
     */
    private synchronized void writeThroughSocket(Message message) throws IOException {
        byte[] msg = message.toSendableBytes();
        
        try {
            outputStream.write(msg);
            outputStream.flush();
        } catch (IOException e) {
            close();
            throw e;
        }
        
    }
    
    /**
     * Closes the socket and {@link OutputStream}, interrupts the sender
     * {@link #thread messageQueue consumer} and resets the {@link #active} and {@link #closed} variables.
     * This should NOT be used to end the connection.
     * {@link Message.MessageTypes#END_CONNECTION END_CONNECTION} or {@link Message.MessageTypes#ERROR}
     * should be used first to prevent an error on the other side
     */
    @Override
    public void close() {
        thread.interrupt();
        
        try {
            socket.close(); // OutputStream will close with socket
        } catch (IOException e) {
            System.out.println("Close Error");
        } finally {
            active = false;
            closed = true;
        }
    }
    
    /**
     * Returns the active state of the Sender
     * @return the active state
     * @see #active
     */
    public boolean isActive() {return active;}
    
    /**
     * Returns the closed state of the Sender
     * @return the closed state
     * @see #closed
     */
    public boolean isClosed() {
        return closed;
    }
    
    /**
     * Starts the thread that sends {@link Message.MessageTypes#WATCHDOG_KICK WATCHDOG_KICKs}
     * regularly
     */
    public void start() {
        if (closed) {throw new IllegalStateException("Sender is closed");}
        thread.start();
    }
    
    /**
     * The sender thread consumes {@link Message Messages} and writes to
     * the {@link OutputStream}. It sends a {@link Message.MessageTypes#WATCHDOG_KICK WATCHDOG_KICK}
     * when no message has been sent for 1 second
     */
    private final Thread thread = new Thread() {
        @Override
        public void run() {
            while (!isInterrupted()) {
                
                Message message;
                try {
                    active = true;
                    
                    message = messageQueue.poll(kickRate, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    break;
                }
                
                if (isInterrupted()) {break;}
                
                if (message == null) {
                    JsonObject WatchdogKick = new JsonObject() {{
                        put("MsgType", "WATCHDOG_KICK");
                    }};
                    Message msg = new Message(WatchdogKick, 3);
                    
                    try {
                        writeThroughSocket(msg);
                    } catch (IOException e) {
                        close();
                        callback.exceptionEncountered(e);
                    }
                    continue;
                }
                try {
                    writeThroughSocket(message);
                } catch (IOException e) {
                    close();
                    callback.exceptionEncountered(e);
                }
            }
        }
    };
}