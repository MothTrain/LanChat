package LanChatElements;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

public class ButtonUI extends BasicButtonUI {
    @Override
    protected void paintButtonPressed(Graphics g, AbstractButton b) {
        if (b.isContentAreaFilled()) {
            Graphics2D g2d = (Graphics2D) g;
            Color savedColor = g2d.getColor();
            g2d.setColor(new Color(0,90,0)); // Darker color
            g2d.fillRect(0, 0, b.getWidth(), b.getHeight());
            g2d.setColor(savedColor);
        }
    }
}
