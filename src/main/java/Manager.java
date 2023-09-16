import LanChatElements.LC_Button;
import LanChatElements.LC_Constants;
import LanChatElements.LC_Panel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

public class Manager {
    
    protected int connectionStage;
    
    
    
    
    
    
    
    
    
    
    
    
    
    static final Color green = LC_Constants.green;
    
    private JFrame frame;

    private CardLayout bodyCards;
    private LC_Panel body;
    
    
    private CardLayout menuBarCards;
    private LC_Panel menuBar;
    
    private LC_Panel createKey;
    private LC_Panel enterKey;
    
    private LC_Panel menuBarStart;
    
    
    public static void main(String[] args) {
        Manager window = new Manager();
    }
    
    
    
    public Manager() {
        setUpJframe();
        connectionStage = 1;
    }
    
    private void setUpJframe() {
        frame = new JFrame() {{
            setTitle("LanChat");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(500, 400);
            setResizable(false);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());
            
            URL iconURL = getClass().getResource("icon.png");
            assert iconURL != null;
            ImageIcon icon = new ImageIcon(iconURL);
            setIconImage(icon.getImage());
        }};
        
        
        bodyCards = new CardLayout();
        
        body = new LC_Panel() {{
            setLayout(bodyCards);
        }};
        
        createKey = new LC_Panel();
        createKey.add(new JTextArea() {{setText("Create Connection Key"); setFont(LC_Constants.buttonFont);}});
        
        enterKey = new LC_Panel();
        enterKey.add(new JTextArea() {{setText("Enter Connection Key"); setFont(LC_Constants.buttonFont);}});
        
        body.add("start", new JTextArea(){{setText("Start"); setFont(LC_Constants.buttonFont);}});
        body.add("createKey", createKey);
        body.add("enterKey", enterKey);
        
        
        menuBarCards = new CardLayout();
        
        menuBar = new LC_Panel() {{
            setLayout(menuBarCards);
        }};
        
        menuBarStart = new LC_Panel() {{
            setLayout(new GridLayout(1, 2));
            setBackground(green);
        }};
        
        LC_Button createKeyButton = new LC_Button() {{
            setText("Create Connection Key");
        }};
        LC_Button enterKeyButton = new LC_Button() {{
            setText("Enter Connection Key");
        }};
        menuBarStart.add(createKeyButton, green);
        menuBarStart.add(enterKeyButton, green);

        menuBar.add(menuBarStart, "start");
        
        LC_Button exitCreateKey = new LC_Button() {{
            setText("Back");
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    menuBarCards.show(menuBar, "start");
                    bodyCards.show(body, "start");
                }
            });
        }};
        menuBar.add(exitCreateKey, "createKey");
        
        LC_Button exitEnterKey = new LC_Button() {{
            setText("Back");
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    menuBarCards.show(menuBar, "start");
                    bodyCards.show(body, "start");
                }
            });
        }};
        menuBar.add(exitEnterKey, "enterKey");
        
        createKeyButton.addActionListener(e -> {
            bodyCards.show(body, "createKey");
            menuBarCards.show(menuBar, "createKey");
        });
        enterKeyButton.addActionListener(e -> {
            bodyCards.show(body, "enterKey");
            menuBarCards.show(menuBar, "enterKey");
        });
        
        frame.add(menuBar, BorderLayout.NORTH);
        frame.add(body, BorderLayout.CENTER);
        
        frame.setVisible(true);
    }
}



