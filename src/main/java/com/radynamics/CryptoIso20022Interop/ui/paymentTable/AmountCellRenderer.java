package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class AmountCellRenderer extends JLabel implements TableCellRenderer {
    private TransformInstruction transformInstruction;
    private TableColumn objectColumn;

    private static final NumberFormat df = DecimalFormat.getInstance();

    public AmountCellRenderer(TransformInstruction transformInstruction, TableColumn objectColumn) {
        this.transformInstruction = transformInstruction;
        this.objectColumn = objectColumn;

        final int DIGITS = 2;
        df.setMinimumFractionDigits(DIGITS);
        df.setMaximumFractionDigits(DIGITS);
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        var amt = (Double) value;
        var obj = (Transaction) table.getModel().getValueAt(row, objectColumn.getModelIndex());

        if (StringUtils.equalsIgnoreCase(obj.getLedger().getNativeCcySymbol(), transformInstruction.getTargetCcy())) {
            setText(amt.toString());
        } else {
            setText(df.format(amt));
        }

        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        setHorizontalAlignment(SwingConstants.RIGHT);
        return this;
    }
}
