import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import LanChatMessages.Message;
import com.github.cliftonlabs.json_simple.JsonObject;
public class Sender extends Thread {
    private final String IPAddress;
    private final int port;
    private final Socket socket;
    private final OutputStream outputStream;
    private final LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>(1);
    
    
    public Sender(String ipAddress, int port) throws IOException {
        IPAddress = ipAddress;
        this.port = port;
        
        socket = new Socket(IPAddress, port);
        outputStream = socket.getOutputStream();
        
        start();
    }
    
    public synchronized void sendMessage(Message message) throws IllegalStateException {
        messageQueue.add(message);
    }
    
    public synchronized void writeThroughSocket(Message message) throws IOException {
        String msgStr = message.toJson();
        
        outputStream.write(msgStr.getBytes(StandardCharsets.UTF_8));
    }
    
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