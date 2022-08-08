package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.Iso4217CurrencyCode;
import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.ui.Consts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class CurrencyCellRenderer extends JLabel implements TableCellRenderer {
    public CurrencyCellRenderer() {
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
        setText(ccy.getCcy());

        // Native currencies like "XRP" don't have an issuer.
        if (ccy.getIssuer() == null) {
            return this;
        }

        setToolTipText(String.format("Issued by %s", ccy.getIssuer().getPublicKey()));

        if (Iso4217CurrencyCode.contains(ccy.getCcy())) {
            setForeground(isSelected ? table.getSelectionForeground() : Consts.ColorIssuedFiatCcy);
        }

        return this;
    }
}
