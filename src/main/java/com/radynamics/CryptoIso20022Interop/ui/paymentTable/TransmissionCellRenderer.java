package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TransmissionCellRenderer extends JLabel implements TableCellRenderer {
    private final static int WIDTH = 16;
    private final static int HEIGHT = 16;

    private final static FlatSVGIcon pending = null;
    private final static FlatSVGIcon success = new FlatSVGIcon("svg/success.svg", WIDTH, HEIGHT);
    private final static FlatSVGIcon error = new FlatSVGIcon("svg/errorDialog.svg", WIDTH, HEIGHT);

    public TransmissionCellRenderer() {
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        var transmission = (TransmissionState) value;

        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setHorizontalAlignment(JLabel.CENTER);
        setIcon(getIconOrNull(transmission));
        return this;
    }

    private static Icon getIconOrNull(TransmissionState value) {
        switch (value) {
            case Pending -> {
                return pending;
            }
            case Success -> {
                return success;
            }
            case Error -> {
                return error;
            }
            default -> throw new IllegalStateException("Unexpected value: " + value);
        }
    }
}
