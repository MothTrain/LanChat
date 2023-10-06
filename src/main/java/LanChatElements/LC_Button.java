package LanChatElements;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
class LC_Button extends JButton {
    public LC_Button() {
        setContentAreaFilled(false); // Make the button transparent
        setUI(new ButtonUI());
        setBackground(LC_Constants.buttonGreen);
        setBorder(null);
        setFont(LC_Constants.buttonFont);
        setForeground(Color.WHITE);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        if (getModel().isArmed()) {
            // Use a darker background when the button is pressed
            g.setColor(LC_Constants.pressedButtonGreen);
        } else {
            g.setColor(getBackground());
        }
        
        // Create a rounded rectangle shape to be used as the button's background
        int width = getWidth();
        int height = getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape roundedRect = new RoundRectangle2D.Float(0, 0, width, height, 15, 15);
        g2d.fill(roundedRect);
        
        super.paintComponent(g);
    }
    
    public Insets getInsets() {
        return new Insets(2,10,4,10);
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        // Remove the border painting for this example
    }
    
}