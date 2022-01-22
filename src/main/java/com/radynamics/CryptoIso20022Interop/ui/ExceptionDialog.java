package com.radynamics.CryptoIso20022Interop.ui;

import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;

public final class ExceptionDialog {
    public static void show(Component parentComponent, Exception e) {
        var msg = e.getMessage() == null || e.getMessage().length() == 0 ? "Unknown exception occured. See error logs for details" : e.getMessage();
        LogManager.getLogger().error(msg, e);
        JOptionPane.showMessageDialog(parentComponent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
