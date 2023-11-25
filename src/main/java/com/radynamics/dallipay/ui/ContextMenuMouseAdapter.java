package com.radynamics.dallipay.ui;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ContextMenuMouseAdapter extends MouseAdapter {
    private final JPopupMenu menu;

    public ContextMenuMouseAdapter(JPopupMenu menu) {
        this.menu = menu;
    }

    public void mousePressed(MouseEvent e) {
        checkPopup(e);
    }

    public void mouseClicked(MouseEvent e) {
        checkPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        checkPopup(e);
    }

    private void checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
