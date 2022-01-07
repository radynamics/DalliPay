package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class HorizontalAlignmentHeaderRenderer implements TableCellRenderer {
    private final int horizontalAlignment;

    public HorizontalAlignmentHeaderRenderer(int horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        var renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        renderer.setHorizontalAlignment(horizontalAlignment);
        return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
    }
}
