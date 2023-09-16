package LanChatMessages;

public enum MessageTypes {
    NEW_CONNECTION(new String[]{"MsgType", "IP", "Username"}, 1),
    DECLINE_CONNECTION(new String[]{"MsgType"}, 2),
    ACCEPT_CONNECTION(new String[]{"MsgType"}, 2),
    MSG(new String[]{"MsgType", "Content"}, 3),
    WATCHDOG_KICK(new String[]{"MsgType"}, 3),
    WATCHDOG_BARK(new String[]{"MsgType"}, 3),
    END_CONNECTION(new String[]{"MsgType"}, 3),
    ERROR(new String[]{"MsgType", "Message"}, 3);
    
    
    MessageTypes(String[] arguments, int stage) {
        this.arguments = arguments;
        this.stage = stage;
    }
    
    public String[] getArguments() {
        return arguments;
    }
    
    public int getStage() {
        return stage;
    }
    
    private final String[] arguments;
    private final int stage;
    
    
}
