import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import LanChatMessages.Message;
import com.github.cliftonlabs.json_simple.JsonObject;
public class Sender extends Thread {
    private final Socket socket;
    private final OutputStream outputStream;
    
    /**
     * Queue to write messages that are to be sent. Maximum capacity is 1
     */
    private final LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>(1);
    
    
    /**
     * Creates socket resources and waits for messages
     *
     * @param ipAddress the IP address of the host
     * @param port the port number of the host
     * @throws IOException if an IOException occurs
     */
    public Sender(String ipAddress, int port) throws IOException {
        
        socket = new Socket(ipAddress, port);
        outputStream = socket.getOutputStream();
        
        start();
    }
    
    /**
     * Adds a message to the {@link Sender#messageQueue} to sent
     * through the assigned connection
     *
     * @param message The message to send
     * @throws IllegalStateException If the sender queue is full
     */
    public synchronized void sendMessage(Message message) throws IllegalStateException {
        messageQueue.add(message);
    }
    
    /**
     * @param message The Json to send
     * @throws IOException If an IOException occurs during {@link OutputStream} writing
     */
    private synchronized void writeThroughSocket(Message message) throws IOException {
        String msgStr = message.toJson();
        
        outputStream.write(msgStr.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Closes the socket and {@link OutputStream} and interrupts
     * the sender {@link #run() messageQueue consumer}
     */
    public void close() {
        interrupt();
        
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Close Error");
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            System.out.println("Close Error");
        }
        
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
                    throw new RuntimeException(e);
                }
                continue;
            }
            try {
                writeThroughSocket(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}