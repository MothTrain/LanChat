import java.io.*;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
public class Sender extends Thread {
    
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
     * The closed boolean indicates if the Sender's {@link #run()} method is working.
     * If closed is true but the sender is connected then the message must be written
     * straight to the socket.
     *
     * @see #connected
     */
    private boolean closed = true;
    
    /**
     * The connected boolean indicates if the socket is open and ready to send messages.
     * An exception must be thrown if connected if false, when a message attempted to be sent.
     */
    private boolean connected = false;
    
    /**
     * Queue to write messages that are to be sent. The queue is consumed
     * by the {@link #run()} method. Maximum capacity is 1
     */
    private final LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>(1);
    
    
    /**
     * Creates a sender instance. This does not start the {@link #run()} so
     * {@link Message.MessageTypes#WATCHDOG_KICK WATCHDOG_KICKing} will not happen
     *
     * @param ipAddress the IP address of the host
     * @param port the port number of the host
     * @param callback the callback for exception reporting
     * @throws IOException if an IOException occurs
     * */
    public Sender(String ipAddress, int port, Managerable callback) throws IOException {
        
        this.callback = callback;

        socket = new Socket(ipAddress, port);
        outputStream = new DataOutputStream(socket.getOutputStream());
        
        connected = true;
    }
    
    /**
     * Sends a message through the assigned connection
     *
     * @param message The message to send
     * @throws IllegalStateException If the sender queue is full
     * or the Sender is closed/not yet open
     * @throws IOException If an IO exception occurs
     *
     * @see #writeThroughSocket(Message)
     * @see #connected
     */
    public synchronized void sendMessage(Message message) throws IllegalStateException, IOException {
        if (!connected) {
            throw new IllegalStateException("The Sender is not connected");
        } else if (closed) {
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
     * {@link #run() messageQueue consumer} and resets the {@link #closed} and {@link #connected} variables.
     * This should NOT be used to end the connection.
     * {@link Message.MessageTypes#END_CONNECTION END_CONNECTION} or {@link Message.MessageTypes#ERROR}
     * should be used first to prevent an error on the other side
     */
    public void close() {
        interrupt();
        
        try {
            socket.close(); // OutputStream will close with socket
        } catch (IOException e) {
            System.out.println("Close Error");
        } finally {
            closed = true;
            connected = false;
        }
    }
    
    /**
     * Returns the closed state of the Sender
     * @return the closed state
     * @see #closed
     */
    public boolean isClosed() {return closed;}
    
    /**
     * Returns the connected state of the Sender
     * @return the connected state
     * @see #connected
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * The sender thread consumes {@link Message Messages} and writes to
     * the {@link OutputStream}. It sends a {@link Message.MessageTypes#WATCHDOG_KICK WATCHDOG_KICK}
     * when no message has been sent for 1 second
     */
    @Override
    public void run() {
        while (!isInterrupted()) {
            
            Message message;
            try {
                closed = false;
                
                message = messageQueue.poll(1000L, TimeUnit.MILLISECONDS);
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
}