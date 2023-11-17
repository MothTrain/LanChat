import LanChatMessages.Message;
import LanChatMessages.MessageTypes;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * The listener class is responsible for listening for messages and relaying them
 * to the Manager
 */
public class Listener implements AutoCloseable {
    private boolean closed = false;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataInputStream dataInputStream;
    private final int port;
    private final Managerable callback;
    private final int timeout;
    
    /**
     * Creates a listener using the port and starts the {@link #thread listening thread},
     * which will begin listening for a connection
     *
     * @param port     The port to listen on
     * @param callback The manager to callback to
     * @param timeout the time in milliseconds before the listener times out and reports
     *                to {@link Managerable#exceptionEncountered(Exception)}
     */
    public Listener(int port, Managerable callback, int timeout) {
        this.port = port;
        this.callback = callback;
        this.timeout = timeout;
        
        thread.start();
    }
    
    /**
     * Closes the listener's {@link #clientSocket}, {@link #serverSocket} and
     * interrupts the listening thread. This will interrupt a listening operation.
     * This should NOT be used to end the connection.
     * {@link MessageTypes#END_CONNECTION} or {@link MessageTypes#ERROR}
     * should be used first to prevent an error on the other side
     */
    @Override
    public void close() {
        thread.interrupt();
        closed = true;
        
        try {
            clientSocket.close();
        } catch (IOException | NullPointerException ignored) {}
        try {
            serverSocket.close();
        } catch (IOException | NullPointerException ignored) {}
    }
    
    /**
     * Calls the manager's {@link Managerable#exceptionEncountered(Exception)
     * exceptionEncountered()} only if the listener is not closed. This is to
     * prevent the listener from throwing an exception when the connection is
     * closed: the other used will send and {@link LanChatMessages.MessageTypes#END_CONNECTION
     * END_CONNECTION}, closing the listener so no unnecessary exceptions are reported.
     * @param e The exception to report
     */
    private void reportException(Exception e) {
        if (!closed) {callback.exceptionEncountered(e);}
    }
    
    
    /**
     * Waits for a message to be received. If the message wait times out.
     * When a message is received it will return the message
     *
     * @return The received message
     * @throws SocketTimeoutException if the message wait times out
     * @throws IOException if an IOException occurs while reading data
     */
    private Message waitForMessage() throws IOException {
        int messageLength = dataInputStream.readInt();
        
        byte[] messageByte = new byte[messageLength];
        int totalBytesRead = 0;
        StringBuilder dataString = new StringBuilder(messageLength);
        
        do {
            int currentBytesRead = dataInputStream.read(messageByte);
            totalBytesRead = currentBytesRead + totalBytesRead;
            
            if (totalBytesRead <= messageLength) {
                dataString.append(new String(
                                messageByte,
                                0,
                                currentBytesRead,
                                StandardCharsets.UTF_8));
            } else {
                dataString.append(new String(
                                messageByte,
                                0,
                                messageLength - totalBytesRead + currentBytesRead,
                                StandardCharsets.UTF_8));
            }
        } while (dataString.length() < messageLength - 1);
        return new Message(dataString.toString() , callback.getConnectionStage());
    }
    
    /**
     * The thread waits for an incoming connection and, once received,
     * will begin waiting for messages using {@link #waitForMessage()}
     * and will send it to the manager. If the wait times out then the
     * thread will report the {@link SocketTimeoutException} through
     * the callback {@link Managerable#exceptionEncountered(Exception)
     * exceptionEncountered()}
     */
    private final Thread thread = new Thread() {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);
                clientSocket = serverSocket.accept();
                
                clientSocket.setSoTimeout(timeout);
                dataInputStream = new DataInputStream(
                        new BufferedInputStream(
                                clientSocket.getInputStream()));
                
                
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                
                while (!isInterrupted()) {
                    Message jsonData = waitForMessage();
                    
                    System.out.println("Received JSON data: " + jsonData.toJson());
                    callback.messageReceived(jsonData);
                }
            } catch (SocketTimeoutException e) {
                reportException(e);
            } catch (IOException e) {
                close();
                reportException(e);
            }
        }
    };
    
    public int getPort() {
        return port;
    }
    
    public String getIP() {
        return clientSocket.getInetAddress().toString();
    }
}
