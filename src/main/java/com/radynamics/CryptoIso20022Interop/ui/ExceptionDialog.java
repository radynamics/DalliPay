package com.radynamics.CryptoIso20022Interop.ui;

import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;

public final class ExceptionDialog {
    public static void show(Component parentComponent, Exception e) {
        LogManager.getLogger().error(e);
        JOptionPane.showMessageDialog(parentComponent, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
