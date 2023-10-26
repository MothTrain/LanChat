package LanChatElements;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class LC_Constants {
    private LC_Constants() {}
    
    public final static Color transparent = new Color(0,0,0,0);
    public final static Color green = new Color(0,135,0);
    public final static Color buttonGreen = new Color(0,123,0);
    public final static Color pressedButtonGreen = new Color(0,110,0);
    public final static Color grey = new Color(100,100,100);
    public final static Color sendersBlue = new Color(0,110,244);
    public final static Color receiversRed = new Color(255, 66, 28);
    
    public final static Font buttonFont = new Font("Arial", Font.PLAIN, 20);
    
    public final static SimpleAttributeSet sendersStyle;
    
    static {
        sendersStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(sendersStyle, Color.BLACK);
        StyleConstants.setFontSize(sendersStyle, 12);
        StyleConstants.setAlignment(sendersStyle, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setFontFamily(sendersStyle, "Arial");
    }
    public final static SimpleAttributeSet sendersNameStyle;
    static {
        sendersNameStyle = new SimpleAttributeSet();
        
        StyleConstants.setForeground(sendersNameStyle, sendersBlue);
        StyleConstants.setFontSize(sendersNameStyle, 12);
        StyleConstants.setAlignment(sendersNameStyle, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setBold(sendersNameStyle, true);
        StyleConstants.setFontFamily(sendersNameStyle, "Arial");
    }
    
    public final static SimpleAttributeSet receivedStyle;
    static {
        receivedStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(receivedStyle, Color.BLACK);
        StyleConstants.setFontSize(receivedStyle, 12);
        StyleConstants.setAlignment(receivedStyle, StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontFamily(receivedStyle, "Arial");
    }
    
    public final static SimpleAttributeSet receivedNameStyle;
    static {
        receivedNameStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(receivedNameStyle, receiversRed);
        StyleConstants.setFontSize(receivedNameStyle, 12);
        StyleConstants.setAlignment(receivedNameStyle, StyleConstants.ALIGN_LEFT);
        StyleConstants.setBold(receivedNameStyle, true);
        StyleConstants.setFontFamily(receivedNameStyle, "Arial");
    }
    
    public final static SimpleAttributeSet informationStyle;
    static {
        informationStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(informationStyle, grey);
        StyleConstants.setFontSize(informationStyle, 10);
        StyleConstants.setAlignment(informationStyle, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontFamily(informationStyle, "Arial");
    }
}
