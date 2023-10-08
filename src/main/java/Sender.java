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
    private final Socket socket;
    private final DataOutputStream outputStream;
    private final Managerable callback;
    
    /**
     * The closed boolean indicates if the Sender is ready to send messages.
     * Send methods should throw {@link IllegalStateException} if closed is true
     */
    private boolean closed = true;
    
    /**
     * Queue to write messages that are to be sent. Maximum capacity is 1
     */
    private final LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>(1);
    
    
    /**
     * Creates socket resources and waits for messages
     *
     * @param ipAddress the IP address of the host
     * @param port the port number of the host
     * @param callback the callback for exception reporting
     * @throws IOException if an IOException occurs
     * @throws TimeoutException if the initialisation of the thread fr
     */
    public Sender(String ipAddress, int port, Managerable callback)
            throws IOException, TimeoutException, InterruptedException {
        
        this.callback = callback;
        
        socket = new Socket(ipAddress, port);
        outputStream = new DataOutputStream(socket.getOutputStream());
        
        start();
        
        wait(500L);
        if (closed) {throw new TimeoutException("Thread start took too long: SHOULD NOT BE POSSIBLE");}
    }
    
    /**
     * Adds a message to the {@link Sender#messageQueue} to sent
     * through the assigned connection
     *
     * @param message The message to send
     * @throws IllegalStateException If the sender queue is full
     * or the Sender is closed/not yet open
     */
    public synchronized void sendMessage(Message message) throws IllegalStateException {
        if (closed) {throw new IllegalStateException("The Sender is closed");}
        messageQueue.add(message);
    }
    
    /**
     * @param message The Json to send
     * @throws IOException If an IOException occurs during {@link OutputStream} writing
     */
    private synchronized void writeThroughSocket(Message message) throws IOException {
        byte[] msg = message.toSendableBytes();
        
        outputStream.write(msg);
        outputStream.flush();
    }
    
    /**
     * Closes the socket and {@link OutputStream} and interrupts
     * the sender {@link #run() messageQueue consumer}
     */
    public void close() {
        interrupt();
        
        try {
            socket.close(); // OutputStream will close with socket
        } catch (IOException e) {
            System.out.println("Close Error");
        } finally {
            closed = true;
        }
    }
    
    /**
     * @return returns the closed state of the Sender
     * @see #closed
     */
    public boolean isClosed() {return closed;}
    
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
                notifyAll();
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