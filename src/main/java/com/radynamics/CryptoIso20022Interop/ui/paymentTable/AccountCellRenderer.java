package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.OtherAccount;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class AccountCellRenderer extends JLabel implements TableCellRenderer {
    public AccountCellRenderer() {
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());

        if (value instanceof IbanAccount) {
            var iban = (IbanAccount) value;
            setText(iban.getFormatted());
        } else if (value instanceof OtherAccount) {
            var iban = (OtherAccount) value;
            setText(iban.getUnformatted());
        } else {
            setText(value.toString());
        }
        return this;
    }
}
