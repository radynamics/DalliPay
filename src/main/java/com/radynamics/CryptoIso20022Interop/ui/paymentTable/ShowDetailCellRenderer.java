package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.ui.Consts;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ShowDetailCellRenderer extends JLabel implements TableCellRenderer {
    public ShowDetailCellRenderer() {
        setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        var text = (String) value;

        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setForeground(isSelected ? table.getSelectionForeground() : Consts.ColorAccent);
        setText(text);
        return this;
    }
}
