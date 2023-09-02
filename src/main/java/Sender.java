import java.io.IOException;
import java.net.Socket;

public class Sender extends Thread {
    private final String IPAddress;
    private final int port;
    private Socket sendingSocket;
    
    
    public Sender(String ipAddress, int port) throws IOException {
        IPAddress = ipAddress;
        this.port = port;
        
        sendingSocket = new Socket(IPAddress, port);
    }
    
}