package com.radynamics.dallipay.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class WaitingForm {
    private static final ResourceBundle res = ResourceBundle.getBundle("i18n." + WaitingForm.class.getSimpleName());

    public static JDialog create(Component parentComponent, String text) {
        var frm = new JDialog(new JFrame(), res.getString("title"), true);
        frm.setSize(400, 200);
        frm.setLocationRelativeTo(parentComponent);

        var box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        {
            var lbl = new JLabel();
            box.add(lbl);
            lbl.setText(res.getString("title"));
            lbl.putClientProperty("FlatLaf.style", "font: 200% $semibold.font");
        }
        box.add(Box.createRigidArea(new Dimension(0, 10)));
        {
            var lbl = new JLabel("<html><body>%s</body></html>".formatted(text));
            box.add(lbl);
        }
        frm.add(box);

        return frm;
    }
}
