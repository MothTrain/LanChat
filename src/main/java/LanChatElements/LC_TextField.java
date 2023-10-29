package LanChatElements;

import javax.swing.*;
import java.awt.*;

public class LC_TextField extends JTextField {
    public LC_TextField() {
        super();
        setHorizontalAlignment(LC_TextField.CENTER);
    }
    
    public Insets getInsets() {
        return LC_Constants.buttonInsets;
    }
}
