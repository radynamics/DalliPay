package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.iso20022.Address;
import com.radynamics.CryptoIso20022Interop.iso20022.AddressFormatter;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class AddressCellRenderer extends JLabel implements TableCellRenderer {
    private final AddressFormatter af = new AddressFormatter();

    public AddressCellRenderer() {
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());

        var obj = (Address) value;
        setText(af.formatSingleLine(obj));
        return this;
    }
}
