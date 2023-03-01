package com.radynamics.dallipay.ui;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class PlaceholderTextArea extends JTextArea {
    @Override
    protected void paintComponent(final Graphics pG) {
        super.paintComponent(pG);

        var value = getClientProperty(FlatClientProperties.PLACEHOLDER_TEXT);
        if (value == null || !isEditable()) {
            return;
        }

        var placeholderText = String.valueOf(value);
        if (placeholderText.length() == 0 || getText().length() > 0) {
            return;
        }

        final Graphics2D g = (Graphics2D) pG;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(getDisabledTextColor());
        g.drawString(placeholderText, getInsets().left, pG.getFontMetrics().getMaxAscent() + getInsets().top);
    }
}
