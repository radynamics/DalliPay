package com.radynamics.dallipay.ui.paymentTable;

import com.radynamics.dallipay.iso20022.AmountFormatter;
import com.radynamics.dallipay.iso20022.Payment;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ResourceBundle;

public class AmountCellRenderer extends JLabel implements TableCellRenderer {
    private final TableColumn objectColumn;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

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
            setToolTipText(res.getString("naNoFxRateFound"));
            return this;
        }

        return this;
    }
}
