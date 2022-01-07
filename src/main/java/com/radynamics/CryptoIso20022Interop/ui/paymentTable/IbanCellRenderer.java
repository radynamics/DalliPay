package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class IbanCellRenderer extends JLabel implements TableCellRenderer {
    public IbanCellRenderer() {
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        var iban = (IbanAccount) value;

        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        setText(iban.getFormatted());
        return this;
    }
}
