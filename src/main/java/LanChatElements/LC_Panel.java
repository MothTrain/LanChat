package LanChatElements;

import javax.swing.*;

class LC_Panel extends JPanel {
    public void add(LC_Button comp) {
        addImpl(new JPanel() {{
            add(comp);
            setOpaque(false);
        }}, null, -1);
    }
    
    public void add(LC_Button comp, Object constraints) {
        addImpl(new JPanel() {{
            add(comp);
            setBackground(LC_Constants.green);
        }}, constraints, -1);
    }
    
    public void wrapAdd(JComponent comp) {
        add(new JPanel() {{add(comp);}});
    }
}
