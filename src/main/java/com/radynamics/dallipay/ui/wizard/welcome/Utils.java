package com.radynamics.dallipay.ui.wizard.welcome;

import javax.swing.*;
import java.awt.*;

public final class Utils {
    public static Component createText(String text) {
        var lbl = createLabel(text);
        lbl.setPreferredSize(new Dimension(200, 44));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private static JLabel createLabel(String text) {
        return new JLabel("<html><body>%s</body></html>".formatted(text.replaceAll("\n", "<br />")));
    }

    public static Component createImageWithText(String text, String imgName) {
        var lbl = createLabel(text);
        lbl.setIcon(new ImageIcon(ClassLoader.getSystemResource("img/wizard/welcome/%s".formatted(imgName))));
        return lbl;
    }
}
