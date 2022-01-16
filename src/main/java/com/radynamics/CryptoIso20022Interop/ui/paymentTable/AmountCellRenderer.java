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

    private static final NumberFormat dfFiat = DecimalFormat.getInstance();
    private static final NumberFormat dfCryptocurrency = DecimalFormat.getInstance();

    public AmountCellRenderer(TransformInstruction transformInstruction, TableColumn objectColumn) {
        this.transformInstruction = transformInstruction;
        this.objectColumn = objectColumn;

        setDigits(dfFiat, 2);
        setDigits(dfCryptocurrency, 6);

        setOpaque(true);
    }

    private static void setDigits(NumberFormat df, int digits) {
        df.setMinimumFractionDigits(digits);
        df.setMaximumFractionDigits(digits);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        setHorizontalAlignment(SwingConstants.RIGHT);

        if (value == null) {
            setText("...");
            setToolTipText("loading...");
            return this;
        }

        var amt = (Double) value;
        var obj = (Transaction) table.getModel().getValueAt(row, objectColumn.getModelIndex());

        var df = StringUtils.equalsIgnoreCase(obj.getLedger().getNativeCcySymbol(), transformInstruction.getTargetCcy())
                ? dfCryptocurrency
                : dfFiat;
        setText(df.format(amt));

        return this;
    }
}
