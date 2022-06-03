package com.radynamics.CryptoIso20022Interop.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
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

    public static String removeEndingLineSeparator(String text) {
        return text != null && text.endsWith("\n") ? text.substring(0, text.lastIndexOf("\n")) : text;
    }

    public static JLabel formatSecondaryInfo(JLabel lbl) {
        lbl.putClientProperty("FlatLaf.styleClass", "small");
        lbl.setForeground(Consts.ColorSmallInfo);
        return lbl;
    }

    public static Image getProductIcon() {
        try {
            return new ImageIcon(ImageIO.read(Utils.class.getClassLoader().getResourceAsStream("img/productIcon.png"))).getImage();
        } catch (IOException e) {
            ExceptionDialog.show(null, e);
            return null;
        }
    }

    public static ImageIcon getScaled(String resourceName, int w, int h) {
        var icon = new ImageIcon(ClassLoader.getSystemResource(resourceName));
        return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_DEFAULT));
    }

    public static void openBrowser(Component parent, URI uri) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException ex) {
                ExceptionDialog.show(parent, ex);
            }
        }
    }

    public static String toHexString(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}
