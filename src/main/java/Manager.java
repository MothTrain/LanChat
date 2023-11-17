import LanChatElements.ConnectionKey;
import LanChatElements.LC_Window;
import LanChatElements.LC_Windowable;
import LanChatMessages.Message;
import com.github.cliftonlabs.json_simple.JsonObject;
import org.apache.logging.log4j.Level;

import java.io.IOException;

public class Manager implements LC_Windowable, Managerable {
    private final LC_Window window = new LC_Window(this);
    private Sender sender;
    private Listener listener;
    private int stage = 0;
    private String Username;
    
    
    public static void main(String[] args) {
        Manager manager = new Manager();
    }
    
    @Override
    public ConnectionKey connectionListen() {
        int port = ConnectionKey.getRandomPort();
        
        ConnectionKey key;
        try {
            key = new ConnectionKey(port);
        } catch (IOException e) {
            ExceptionLogger.log(Level.WARN, e);
            return null;
        }
        
        listener = new Listener(port, this, 1500);
        stage = 1;
        
        return key;
    }
    
    @Override
    public void connectTo(String connectionKey, String username) {
        stage = 1;
        
        ConnectionKey key;
        try {
            key = new ConnectionKey(connectionKey);
            
            sender = new Sender(key.IP, key.port, this, 1000);
            
            listener = new Listener(sender, this, 0);
        
            sender.sendMessage(new Message(new JsonObject() {{
                put("MsgType", "NEW_CONNECTION");
                put("Username", username);
            }}, stage));
            
        } catch (IOException e) {
            window.resetAndDisplayError("Couldn't connect: " +
                    "check your \ninternet connection and try again. "
                    + "[" + ExceptionLogger.log(Level.WARN, e) + "]");
            
            endConnectionByError("Connection Error (IO)");
        }
        
        
        
        stage = 2;
    }
    
    @Override
    public void cancelListen() {
        try {
            listener.close();
            listener = null;
        } catch (NullPointerException ignored) {}
        
        stage = 0;
    }
    
    @Override
    public void sendMessage(String message) throws IllegalStateException {
        try {
            sender.sendMessage(new Message(new JsonObject() {{
                put("MsgType", "MSG");
                put("Content", message);
            }}, stage));
        } catch (IOException e) {
            window.resetAndDisplayError("Couldn't send: " +
                    "check your \ninternet connection and try again. "
                    + "[" +ExceptionLogger.log(Level.ERROR, e) + "]");
            
            endConnectionByError("Connection Error (IO)");
            return;
        }
        window.addSentMessage(message);
    }
    
    @Override
    public void endConnection() {
        try {
            sender.sendMessage(new Message(new JsonObject() {{
                put("MsgType", "END_CONNECTION");
            }}, stage));
        } catch (NullPointerException ignored) {}
        catch (IOException e) {
            ExceptionLogger.log(Level.INFO, e);
        }
        
        close();
    }
    
    private void endConnectionByError(String msg) {
        try {
            sender.sendMessage(new Message(new JsonObject() {{
                put("MsgType", "ERROR");
                put("Message", msg);
            }}, stage));
        
        } catch (NullPointerException ignored) {}
        catch (IOException e) {
            ExceptionLogger.log(Level.INFO, e);
        }
        
        close();
    }
    
    private void close() {
        try {
            listener.close();
            listener = null;
        } catch (NullPointerException ignored) {}
        try {
            sender.close();
            sender = null;
        } catch (NullPointerException ignored) {}
        
        stage = 0;
    }
    
    @Override
    public void allowConnection(boolean allowed, String username) {
        
        if (allowed) {
            try {
                stage = 2;
                
                sender = new Sender(listener
                , this, 1000);
                sender.sendMessage(new Message(new JsonObject() {{
                    put("MsgType", "ACCEPT_CONNECTION");
                    put("Username", username);
                }}, stage));
                
                stage = 3;
                
                window.connectionMade(Username);
                
            } catch (IOException e) {
                window.resetAndDisplayError("Couldn't Connect: " +
                        "check your \ninternet connection and try again. "
                        + "[" +ExceptionLogger.log(Level.WARN, e) + "]");
                
                endConnectionByError("Connection Error (IO)");
            }
        } else {
            
            try {
                stage = 2;
                
                sender = new Sender(listener
                        , this, 1000);
                sender.sendMessage(new Message(new JsonObject() {{
                    put("MsgType", "DECLINE_CONNECTION");
                }}, stage));
                
                sender.close();
                sender = null;
            } catch (IOException e) {
                window.resetAndDisplayError("Couldn't Connect: " +
                        "check your \ninternet connection and try again. "
                        + "[" +ExceptionLogger.log(Level.WARN, e) + "]");
                
                endConnectionByError("Connection Error (IO)");
            }
        }
    }
    
    @Override
    public void messageReceived(Message message) {
        switch (message.type) {
            case NEW_CONNECTION -> {
                window.checkAllowConnection(message.get("Username"));
                
                listener.getIP();
                Username = message.get("Username");
                
            } case ACCEPT_CONNECTION -> {
                window.connectionMade(message.get("Username"));
                
                Username = message.get("Username");
                
                stage = 3;
            } case DECLINE_CONNECTION -> {
                window.connectionDeclined();
                
                sender.close();
                sender = null;
            } case MSG ->
                    window.addReceivedMessage(message.get("Content"), Username);
            case END_CONNECTION ->
                    window.resetAndDisplayError("The other user ended \n the connection");
            case ERROR ->
                    window.resetAndDisplayError("The other user encountered \n a" +
                    " fatal error (" + message.get("Message") + ")");
        }
    }
    
    @Override
    public int getConnectionStage() {
        return stage;
    }
    
    @Override
    public void exceptionEncountered(Exception e) {
        ExceptionLogger.log(Level.ERROR, e);
    }
}
