package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.iso20022.AmountFormatter;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

public class AmountCellRenderer extends JLabel implements TableCellRenderer {
    private final TableColumn objectColumn;

    public AmountCellRenderer(TableColumn objectColumn) {
        this.objectColumn = objectColumn;

        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        setHorizontalAlignment(SwingConstants.RIGHT);
        setToolTipText("");

        var obj = (Payment) table.getModel().getValueAt(row, objectColumn.getModelIndex());
        setText(AmountFormatter.formatAmt(obj));
        setToolTipText("");
        if (obj.isAmountUnknown()) {
            setToolTipText("Not available due no exchange rate was found at this point in time.");
            return this;
        }

        return this;
    }
}
