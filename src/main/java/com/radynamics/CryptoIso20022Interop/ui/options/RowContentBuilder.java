package com.radynamics.CryptoIso20022Interop.ui.options;

import javax.swing.*;
import java.awt.*;

public class RowContentBuilder {
    private final JPanel container;
    private final SpringLayout contentLayout;

    public RowContentBuilder(JPanel container, SpringLayout contentLayout) {
        this.container = container;
        this.contentLayout = contentLayout;
    }

    void addRowContent(int top, Component component) {
        final int paddingWest = 150;
        contentLayout.putConstraint(SpringLayout.WEST, component, paddingWest, SpringLayout.WEST, container);
        contentLayout.putConstraint(SpringLayout.NORTH, component, top, SpringLayout.NORTH, container);
        container.add(component);
    }

    JLabel addRowLabel(int top, String text) {
        var lbl = new JLabel(text);
        contentLayout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, container);
        contentLayout.putConstraint(SpringLayout.NORTH, lbl, top, SpringLayout.NORTH, container);
        lbl.setOpaque(true);
        container.add(lbl);
        return lbl;
    }
}
