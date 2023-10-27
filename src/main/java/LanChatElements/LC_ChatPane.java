package LanChatElements;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.StringBufferInputStream;
import java.text.AttributedString;
import java.util.ArrayList;

public class LC_ChatPane extends JScrollPane{
    private final JTextPane textPane = new JTextPane() {{
        setEditable(false);
    }};
    
    public LC_ChatPane() {
        setViewportView(textPane);
        
        getVerticalScrollBar().setBlockIncrement(10);
        getVerticalScrollBar().setUnitIncrement(15);
    }
    
    StyledDocument doc = textPane.getStyledDocument();
    
    
    public void reset() {
        textPane.setText("");
    }
    
    public void addSentMessage(String msg) {
        String username = "You:  ";
        int lengthAtStart = doc.getLength();
        
        try {
            doc.insertString(lengthAtStart, username, LC_Constants.sendersNameStyle);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        doc.setParagraphAttributes(lengthAtStart, username.length(),
                LC_Constants.sendersNameStyle, false);
        
        lengthAtStart = doc.getLength();
        
        try {
            doc.insertString(lengthAtStart, wrapString(msg,
                    LC_Constants.sendersStyle,
                            280) + "\n",
                    LC_Constants.sendersStyle);
            
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        doc.setParagraphAttributes(lengthAtStart, msg.length(),
                LC_Constants.sendersStyle, false);
        
    }
    
    
    public void addReceivedMessage(String msg, String username) {
        int lengthAtStart = doc.getLength();
        username = username + ":  ";
        
        try {
            doc.insertString(lengthAtStart,
                    username,
                    LC_Constants.receivedNameStyle);
            
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        doc.setParagraphAttributes(lengthAtStart, username.length(),
                LC_Constants.receivedNameStyle, false);
        
        lengthAtStart = doc.getLength();
        
        try {
            doc.insertString(lengthAtStart,
                    wrapString(msg, LC_Constants.receivedStyle,
                            280) + "\n",
                    LC_Constants.receivedStyle);
            
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        doc.setParagraphAttributes(lengthAtStart, msg.length(),
                LC_Constants.receivedStyle, false);
        
    }
    
    
    public void addInformationMessage(String msg) {
        int lengthAtStart = doc.getLength();
        
        try {
            doc.insertString(lengthAtStart, wrapString(msg,
                            LC_Constants.informationStyle,
                            280) + "\n",
                    LC_Constants.informationStyle);
            
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
        doc.setParagraphAttributes(lengthAtStart, msg.length(),
                LC_Constants.informationStyle, false);
        
    }
    
    
    private String wrapString(String string, SimpleAttributeSet style, int wrapLength) {
        String[] words = string.split(" ");
        Font font = getFont(style);
        
        FontMetrics fontMetrics = generateFontMetric(font);
        
        int[] wordLengths = new int[words.length];
        
        for (int i=0; i<words.length; i++) {
            wordLengths[i] = fontMetrics.stringWidth(words[i]);
        }
        
        int spaceWidth = fontMetrics.stringWidth(" ");
        
        ArrayList<StringBuilder> stringBuilderLines = new ArrayList<>();
        stringBuilderLines.add(new StringBuilder());
        
        for (int i = 0; i < words.length; i++) {
            if (fontMetrics.stringWidth(stringBuilderLines.get(stringBuilderLines.size()-1)
                    .toString()) + wordLengths[i] <= wrapLength) {
                
                stringBuilderLines.get(stringBuilderLines.size() - 1)
                        .append(words[i]).append(" ");
            
            } else if (wordLengths[i] >= wrapLength) {
                for (String line : splitLargeString(words[i], fontMetrics, wrapLength)) {
                    stringBuilderLines.add(new StringBuilder(line));
                }
                stringBuilderLines.get(stringBuilderLines.size()-1).append(" ");
                
            } else if (fontMetrics.stringWidth(stringBuilderLines.get(stringBuilderLines.size()-1)
                    .toString()) + wordLengths[i] >= wrapLength) {
                stringBuilderLines.add(new StringBuilder());
                
                stringBuilderLines.get(stringBuilderLines.size() - 1)
                        .append(words[i]).append(" ");
                
            } else {throw new RuntimeException("condition gap");}
        }
        
        String output = "";
        
        for (StringBuilder i : stringBuilderLines) {
            output = output.concat(i.toString() + "\n");
        }
        return output;
    }
    
    private FontMetrics generateFontMetric(Font font) {
        Graphics g = textPane.getGraphics();
        
        return g.getFontMetrics();
    }
    
    private static Font getFont(SimpleAttributeSet style) {
        return new Font(
                StyleConstants.getFontFamily(style),
                StyleConstants.isBold(style) ? Font.BOLD : Font.PLAIN,
                StyleConstants.getFontSize(style)
        );
    }
    
    private ArrayList<String> splitLargeString(String longString,
                                               FontMetrics fontMetrics,
                                               int wrapLength) {
        
        StringBuilder line = new StringBuilder();
        StringBuilder string = new StringBuilder();
        string.append(longString);
        
        
        while (
                fontMetrics.stringWidth(line + (string.substring(string.length()-1)))
                        <= wrapLength &&
                        !string.isEmpty()) {
            
            line.append(string.substring(0,1));
            
            string.deleteCharAt(0);
            
            if (string.isEmpty()) {
                break;
            }
        }
        
        if (string.isEmpty()) {
            return new ArrayList<>() {{add(line.toString());}};
        } else {
            return new ArrayList<>() {{
                add(line.toString());
                addAll(splitLargeString(string.toString(), fontMetrics, wrapLength));
            }};
        }
        
    }
}
