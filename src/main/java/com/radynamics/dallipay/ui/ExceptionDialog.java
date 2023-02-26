package com.radynamics.dallipay.ui;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public final class ExceptionDialog {
    private static final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    public static void show(Component parentComponent, Throwable e) {
        show(parentComponent, e, null);
    }

    public static void show(Component parentComponent, Throwable e, String prefix) {
        var errorMsg = e.getMessage() == null || e.getMessage().length() == 0 ? res.getString("unknownException") : e.getMessage();
        var msg = StringUtils.isEmpty(prefix) ? errorMsg : String.format("%s%s%s", prefix, System.lineSeparator(), errorMsg);
        LogManager.getLogger(ExceptionDialog.class).error(msg, e);
        JOptionPane.showMessageDialog(parentComponent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
