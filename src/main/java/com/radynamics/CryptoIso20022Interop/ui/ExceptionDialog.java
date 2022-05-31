package com.radynamics.CryptoIso20022Interop.ui;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;

public final class ExceptionDialog {
    public static void show(Component parentComponent, Exception e) {
        show(parentComponent, e, null);
    }

    public static void show(Component parentComponent, Exception e, String prefix) {
        var errorMsg = e.getMessage() == null || e.getMessage().length() == 0 ? "Unknown exception occured. See error logs for details" : e.getMessage();
        var msg = StringUtils.isEmpty(prefix) ? errorMsg : String.format("%s\n%s", prefix, errorMsg);
        LogManager.getLogger(ExceptionDialog.class).error(msg, e);
        JOptionPane.showMessageDialog(parentComponent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
