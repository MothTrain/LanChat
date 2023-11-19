import LanChatMessages.Message;
import LanChatMessages.MessageTypes;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
    
    /**
     * Creates a listener using the port and starts the {@link #thread listening thread},
     * which will begin listening for a connection
     *
     * @param port     The port to listen on
     * @param callback The manager to callback to
     */
    public Listener(int port, Managerable callback) {
        this.port = port;
        this.callback = callback;
        
        thread.start();
    }
    /**
     * Creates a listener using the socket of a connected sender. This starts the
     * {@link #thread listening thread}. It will not listen for a connection as it
     * has one from the sender
     *
     * @param sender The connected sender, which socket it will use
     * @param callback The manager to callback to
     */
    public Listener(Sender sender, Managerable callback) throws IOException {
        this.clientSocket = sender.getSocket();
        dataInputStream = new DataInputStream(
                new BufferedInputStream(
                        clientSocket.getInputStream()));
        this.port = sender.getPort();
        
        this.callback = callback;
        
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
     * Sets the length that read operations will take to timeout.
     * This will be communicated to the manager by a call to
     * {@link Managerable#exceptionEncountered(Exception)} with
     * {@link SocketTimeoutException}
     * @param timeout Socket timeout length
     * @throws SocketException If an IOException occurs, while setting the
     * socket timeout
     */
    public void setTimeout(int timeout) throws SocketException {
        clientSocket.setSoTimeout(timeout);
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
                if (clientSocket == null) {
                    serverSocket = new ServerSocket(port);
                    System.out.println("Listener: Listening");
                    clientSocket = serverSocket.accept();
                    System.out.println("Listener: Made connection: " + clientSocket.getInetAddress());
                }
                
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
    
    /**
     * Returns the socket that the Listener is using. This may be {@code null} if the listener
     * is not connected.
     *
     * @apiNote Managers SHOULD NOT use this as a means to perform its own
     * operations. It should only be used by the listener {@link Sender#Sender(Listener, Managerable, long)}
     * @return The listener's socket or {@code null} if the listener is not connected
     */
    public Socket getSocket() {
        return clientSocket;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getIP() {
        return clientSocket.getInetAddress().toString().substring(1);
    }
}
