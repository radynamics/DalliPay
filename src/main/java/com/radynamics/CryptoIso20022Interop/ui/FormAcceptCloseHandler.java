package com.radynamics.CryptoIso20022Interop.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class FormAcceptCloseHandler {
    private final JDialog dialog;

    public FormAcceptCloseHandler(JDialog dialog) {
        if (dialog == null) throw new IllegalArgumentException("Parameter 'dialog' cannot be null");
        this.dialog = dialog;
    }

    public void configure() {
        var cancelDialog = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        };
        dialog.getRootPane().registerKeyboardAction(cancelDialog, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        var acceptDialog = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accept();
            }
        };
        dialog.getRootPane().registerKeyboardAction(acceptDialog, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }


    public void accept() {
        close();
    }

    public void close() {
        dialog.dispose();
    }
}
