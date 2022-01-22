package com.radynamics.CryptoIso20022Interop.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public final class Utils {
    public static final NumberFormat createFormatFiat() {
        var df = DecimalFormat.getInstance();
        setDigits(df, 2);
        return df;
    }

    public static final NumberFormat createFormatLedger() {
        var df = DecimalFormat.getInstance();
        setDigits(df, 6);
        return df;
    }

    private static void setDigits(NumberFormat df, int digits) {
        df.setMinimumFractionDigits(digits);
        df.setMaximumFractionDigits(digits);
    }

    public static final DateTimeFormatter createFormatDate() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
    }

    public static JLabel createLinkLabel(Window owner, String text) {
        var lbl = new JLabel(text);
        lbl.setForeground(Consts.ColorAccent);
        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                owner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                owner.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
        return lbl;
    }
}
