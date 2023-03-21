package com.radynamics.dallipay.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;

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
import java.util.ArrayList;
import java.util.Arrays;

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

    public static JTextArea formatLabel(JTextArea lbl) {
        lbl.setEditable(false);
        lbl.setHighlighter(null);
        lbl.setLineWrap(true);
        lbl.setWrapStyleWord(true);
        lbl.setMargin(new Insets(0, 0, 0, 0));
        return lbl;
    }

    public static JLabel createLinkLabel(JComponent owner, String text) {
        return createLinkLabel(owner, text, true);
    }

    public static JLabel createLinkLabel(JComponent owner, String text, boolean enabled) {
        var lbl = new JLabel(text);
        lbl.setEnabled(enabled);
        lbl.setForeground(Consts.ColorAccent);

        if (!enabled) {
            return lbl;
        }

        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                owner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                owner.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                lbl.requestFocus();
            }
        });
        return lbl;
    }

    public static void setRolloverIcon(JToggleButton button) {
        var icon = button.getIcon();
        if (icon == null) {
            return;
        }

        var rolloverIcon = new FlatSVGIcon((FlatSVGIcon) icon);
        rolloverIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> Consts.ColorHoover));
        button.setRolloverIcon(rolloverIcon);

        // Ensure regular icon is shown after click.
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                button.setSelected(false);
            }
        });

    }

    public static String removeEndingLineSeparator(String text) {
        return text != null && text.endsWith(System.lineSeparator()) ? text.substring(0, text.lastIndexOf(System.lineSeparator())) : text;
    }

    public static String toMultilineText(String[] lines) {
        var sb = new StringBuilder();
        for (var l : lines) {
            sb.append(String.format("%s%s", l, System.lineSeparator()));
        }
        return removeEndingLineSeparator(sb.toString());
    }

    public static String[] fromMultilineText(String text) {
        var lines = new ArrayList<>(Arrays.asList(text.split("\\r?\\n")));
        lines.removeAll(Arrays.asList(""));
        return lines.toArray(new String[0]);
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

    public static void patchTabBehavior(JTextArea c) {
        c.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        c.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
    }

    public static JFrame createDummyForm() {
        var frm = new JFrame("DalliPay");
        frm.setIconImage(getProductIcon());
        frm.setUndecorated(true);
        frm.setVisible(true);
        frm.setLocationRelativeTo(null);
        return frm;
    }
}
