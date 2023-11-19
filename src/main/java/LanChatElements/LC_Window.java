package LanChatElements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;


public class LC_Window extends JFrame {
    private final LC_Window This;
    private final LC_Windowable callback;
    
    public LC_Window(LC_Windowable callback) {
        This = this;
        this.callback = callback;
        setUpJframe();
        
        URL iconURL = getClass().getResource("/Error.png");
        if (iconURL != null) {
            error = new ImageIcon(iconURL);
        } else {
            error = null;
        }
    }
    
    private void setUpJframe() {
        setTitle("LanChat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        
        URL iconURL = getClass().getResource("/icon.png");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            setIconImage(icon.getImage());
        }
        
        
        bodyCards = new CardLayout();
        
        body = new LC_Panel() {{
            setLayout(bodyCards);
        }};
        
        LC_Panel createKey = new LC_Panel() {{
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(CENTER_ALIGNMENT);
            
            wrapAdd(new JTextField() {{
                setText("Connection Key:");
                
                setFont(LC_Constants.buttonFont);
                setEditable(false);
                setBorder(null);
                setAlignmentX(Component.CENTER_ALIGNMENT);
                setMaximumSize(getPreferredSize());
            }});
            wrapAdd(keyDisplay = new JTextField() {{
                setText("Key");
                
                setBorder(LC_Constants.border);
                setFont(LC_Constants.buttonFont);
                setEditable(false);
                setAlignmentX(Component.CENTER_ALIGNMENT);
                setMaximumSize(getPreferredSize());
            }});
            
            wrapAdd(new JTextField() {{
                setText("Uncondensed Connection Key:");
                
                setFont(LC_Constants.buttonFont);
                setEditable(false);
                setBorder(null);
                setAlignmentX(Component.CENTER_ALIGNMENT);
                setMaximumSize(getPreferredSize());
            }});
            
            wrapAdd(uncondensedKeyDisplay = new JTextField() {{
                setText("Uncondensed Key");
                
                setBorder(LC_Constants.border);
                setFont(LC_Constants.buttonFont);
                setEditable(false);
                setAlignmentX(Component.CENTER_ALIGNMENT);
                setMaximumSize(getPreferredSize());
            }});
            
            wrapAdd(new JTextField() {{
                setText("Username:");
                
                setFont(LC_Constants.buttonFont);
                setEditable(false);
                setBorder(null);
                setAlignmentX(Component.CENTER_ALIGNMENT);
                setMaximumSize(getPreferredSize());
            }});
            
            wrapAdd(usernameInputCreateKey = new JTextField() {{
                setPreferredSize(new Dimension(200, LC_Constants.buttonFont.getSize()+10));
                setHorizontalAlignment(CENTER);
                setFont(LC_Constants.buttonFont);
                setBorder(LC_Constants.border);
                setOpaque(false);
            }});
            
            wrapAdd(new JTextField() {{
                setText("Now Listening For Incoming Connections...");
                
                setBorder(null);
                setEditable(false);
            }});
        }};
        
        
        LC_Panel enterKey = new LC_Panel() {{
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            wrapAdd(new JTextField() {{
                setText("Enter Connection Key:");
                
                setFont(LC_Constants.buttonFont);
                setEditable(false);
                setBorder(null);
            }});
            
            wrapAdd(connectionKeyInput = new JTextField() {{
                setPreferredSize(new Dimension(200, LC_Constants.buttonFont.getSize()+10));
                setBorder(LC_Constants.border);
                setFont(LC_Constants.buttonFont);
                setOpaque(false);
                setHorizontalAlignment(CENTER);
            }});
            
            wrapAdd(new JTextField() {{
                setText("Enter Username:");
                
                setFont(LC_Constants.buttonFont);
                setEditable(false);
                setBorder(null);
            }});
            
            wrapAdd(usernameInputEnterKey = new JTextField() {{
                setPreferredSize(new Dimension(200, LC_Constants.buttonFont.getSize()+10));
                setHorizontalAlignment(CENTER);
                setFont(LC_Constants.buttonFont);
                setBorder(LC_Constants.border);
                setOpaque(false);
            }});
            
            add(new LC_Button() {{
                setText("Connect");
                
                addActionListener(e -> callback.connectTo(connectionKeyInput.getText(), usernameInputEnterKey.getText()));
            }});
        }};
        
        
        LC_Panel chat = new LC_Panel() {{
            setLayout(new BorderLayout());
        }};
        
        chat.add(usernameArea = new LC_TextField() {{
            setEditable(false);
            setBackground(LC_Constants.green);
            setText("Username");
            
            setFont(LC_Constants.buttonFont);
            setForeground(Color.WHITE);
            
            setBorder(null);
            
        }}, BorderLayout.NORTH);
        
        
        chat.add(chatPane = new LC_ChatPane(), BorderLayout.CENTER);
        
        chat.add(new LC_Panel() {{
            setLayout(new BorderLayout());
            setBackground(LC_Constants.green);
            
            add(messageInput = new LC_MessageInput(150) {{
                setFont(LC_Constants.defaultFont);
                setBackground(Color.WHITE);
            }}, BorderLayout.CENTER);
            
            add(new LC_Button() {{
                setText("Send");
                setMnemonic(KeyEvent.VK_ENTER);
                
                addActionListener(e -> {
                    if (messageInput.getText().isEmpty()) {return;}
                    
                    callback.sendMessage(messageInput.getText());
                    messageInput.setText("");
                    
                });
                
            }}, BorderLayout.EAST);
        }}, BorderLayout.SOUTH);
        
        LC_Panel allowConnection = new LC_Panel() {{
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            wrapAdd(allowConnectionText = new JTextField() {{
                setEditable(false);
                setFont(LC_Constants.buttonFont);
                setBorder(null);
            }});
            
            add(new LC_Panel() {{
                wrapAdd(new LC_Button() {{
                    setText("Decline");
                    
                    setBackground(LC_Constants.receiversRed);
                    
                    addActionListener(e -> {
                        callback.allowConnection(false, null);
                        
                        bodyCards.show(body, "createKey");
                        menuBarCards.show(menuBar, "createKey");
                    });
                }});
                
                wrapAdd(new LC_Button() {{
                    setText("Accept");
                    
                    addActionListener(e -> callback.allowConnection(true, usernameInputCreateKey.getText()));
                    usernameInputCreateKey.setText("");
                }});
            }});
            
        }};
        
        LC_Panel start = new LC_Panel() {{
            add(new JTextField(){{
                setText("To get started, choose to create a");
                setHorizontalAlignment(CENTER);
                setOpaque(false);
                setFont(LC_Constants.buttonFont);
                setEditable(false);
                setBorder(null);
            }});
            add(new JTextField(){{
                setText("connection key or input a key");
                setHorizontalAlignment(CENTER);
                setOpaque(false);
                setFont(LC_Constants.buttonFont);
                setEditable(false);
                setBorder(null);
            }});
        }};
        
        body.add(start, "start");
        body.add(createKey, "createKey");
        body.add(enterKey, "enterKey");
        body.add(chat, "chat");
        body.add(allowConnection, "allowConnection");
        
        
        menuBarCards = new CardLayout();
        
        menuBar = new LC_Panel() {{
            setLayout(menuBarCards);
        }};
        
        LC_Panel menuBarStart = new LC_Panel() {{
            setLayout(new GridLayout(1, 2));
            setBackground(LC_Constants.green);
        }};
        
        LC_Button createKeyButton = new LC_Button() {{
            setText("Create Connection Key");
        }};
        LC_Button enterKeyButton = new LC_Button() {{
            setText("Enter Connection Key");
        }};
        menuBarStart.add(createKeyButton, LC_Constants.buttonGreen);
        menuBarStart.add(enterKeyButton, LC_Constants.buttonGreen);
        
        menuBar.add(menuBarStart, "start");
        
        LC_Button exitCreateKey = new LC_Button() {{
            setText("Cancel");
            
            addActionListener(e -> {
                callback.cancelListen();
                
                menuBarCards.show(menuBar, "start");
                bodyCards.show(body, "start");
            });
        }};
        
        LC_Button exitEnterKey = new LC_Button() {{
            setText("Cancel");
            addActionListener(e -> {
                menuBarCards.show(menuBar, "start");
                bodyCards.show(body, "start");
            });
        }};
        
        LC_Button endConnection = new LC_Button() {{
            setText("End Connection");
            
            addActionListener(e -> {
                callback.endConnection();
                
                menuBarCards.show(menuBar, "start");
                bodyCards.show(body, "start");
            });
        }};
        
        menuBar.add(exitCreateKey, "createKey");
        menuBar.add(exitEnterKey, "enterKey");
        menuBar.add(endConnection, "chat");
        menuBar.add(new LC_Panel(), "allowConnection");
        
        createKeyButton.addActionListener(e -> {
            ConnectionKey key = callback.connectionListen();
            if (key == null) {
                JOptionPane.showMessageDialog(This, "Couldn't connect to the network." +
                                "\nCheck your internet and try again",
                        "Aw Snap!", JOptionPane.ERROR_MESSAGE, error);
                return;
            }
            
            keyDisplay.setText(key.ConnectionKey);
            uncondensedKeyDisplay.setText(key.uncondensedConnectionKey);
            
            bodyCards.show(body, "createKey");
            menuBarCards.show(menuBar, "createKey");
        });
        
        enterKeyButton.addActionListener(e -> {
            bodyCards.show(body, "enterKey");
            menuBarCards.show(menuBar, "enterKey");
        });
        
        add(menuBar, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        
        
        setVisible(true);
    }
    
    
    public void connectionMade(String userName) {
        chatPane.reset();
        
        usernameArea.setText(userName);
        
        messageInput.setText("");
        
        connectionKeyInput.setText("");
        usernameInputEnterKey.setText("");
        usernameInputCreateKey.setText("");
        
        
        menuBarCards.show(menuBar, "chat");
        bodyCards.show(body, "chat");
    }
    
    public void addSentMessage(String msg) {
        chatPane.addSentMessage(msg);
    }
    public void addReceivedMessage(String msg, String username) {
        chatPane.addReceivedMessage(msg, username);
    }
    public void addInformationMessage(String msg) {
        chatPane.addInformationMessage(msg);
    }
    
    public void checkAllowConnection(String username) {
        allowConnectionText.setText("\""+ username + "\" is requesting a connection with you");
        
        bodyCards.show(body, "allowConnection");
        menuBarCards.show(menuBar,"allowConnection");
    }
    
    public void resetAndDisplayError(String errMsg) {
        bodyCards.show(body, "start");
        menuBarCards.show(menuBar, "start");
        
        JOptionPane.showMessageDialog(this, errMsg, "Aw Snap!",
                JOptionPane.ERROR_MESSAGE, error);
    }
    
    public void connectionDeclined() {
        bodyCards.show(body,"enterKey");
        menuBarCards.show(menuBar, "enterKey");
    }
    
    
    private CardLayout bodyCards;
    private LC_Panel body;
    
    private CardLayout menuBarCards;
    private LC_Panel menuBar;
    
    private JTextField keyDisplay;
    private JTextField uncondensedKeyDisplay;
    
    private JTextField usernameArea;
    private LC_ChatPane chatPane;
    private LC_MessageInput messageInput;
    
    private JTextField usernameInputCreateKey;
    
    private JTextField usernameInputEnterKey;
    private JTextField connectionKeyInput;
    
    private JTextField allowConnectionText;
    
    private final ImageIcon error;
}