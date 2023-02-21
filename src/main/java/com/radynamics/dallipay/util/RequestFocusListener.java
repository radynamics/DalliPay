package com.radynamics.dallipay.util;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class RequestFocusListener implements AncestorListener {
    @Override
    public void ancestorAdded(AncestorEvent e) {
        final AncestorListener al = this;
        SwingUtilities.invokeLater(() -> {
            var c = e.getComponent();
            c.requestFocusInWindow();
            c.removeAncestorListener(al);
        });
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
        // do nothing
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
        // do nothing
    }
}
