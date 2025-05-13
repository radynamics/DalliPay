package com.radynamics.dallipay.ui;

import javax.swing.*;

public class NotificationBarEntry {
    private JPanel panel;

    public NotificationBarEntry(JPanel pnl) {
        setPanel(pnl);
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
    }

    public JPanel getPanel() {
        return panel;
    }
}
