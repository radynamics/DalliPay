package com.radynamics.dallipay.ui.paymentTable;

import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.ui.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

public class CurrencyCellRenderer extends JLabel implements TableCellRenderer {
    private final TableColumn objectColumn;

    public CurrencyCellRenderer(TableColumn objectColumn) {
        this.objectColumn = objectColumn;

        setOpaque(true);
        setBorder(new EmptyBorder(0, 3, 0, 0));
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());

        setText(value.toString());
        setToolTipText("");

        if (!(value instanceof Currency)) {
            return this;
        }

        var ccy = (Currency) value;
        setText(ccy.getCode());

        var obj = (Payment) table.getModel().getValueAt(row, objectColumn.getModelIndex());

        var ccyFormatter = new CurrencyFormatter(obj.getLedger().getInfoProvider());
        ccyFormatter.format(this, ccy);
        if (isSelected) {
            setForeground(table.getSelectionForeground());
        }

        return this;
    }
}
