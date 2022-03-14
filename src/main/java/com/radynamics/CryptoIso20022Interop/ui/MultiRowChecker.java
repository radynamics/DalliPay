package com.radynamics.CryptoIso20022Interop.ui;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Function;

public class MultiRowChecker {
    private final JTable table;
    private final TableColumn checkboxColumn;
    private final Function<Integer, Boolean> rowStateChangeable;

    public MultiRowChecker(JTable table, TableColumn checkboxColumn, Function<Integer, Boolean> rowStateChangeable) {
        this.table = table;
        this.checkboxColumn = checkboxColumn;
        this.rowStateChangeable = rowStateChangeable;

        final String solve = "updateCheckedState";
        var key = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        table.getInputMap(JTable.WHEN_FOCUSED).put(key, solve);
        table.getActionMap().put(solve, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCheckBoxState();
            }
        });
    }

    private void updateCheckBoxState() {
        var rowIndicies = table.getSelectedRows();
        if (rowIndicies.length == 0) {
            return;
        }

        var col = checkboxColumn.getModelIndex();
        var checked = (boolean) table.getModel().getValueAt(rowIndicies[0], col);
        for (var row : rowIndicies) {
            if (!rowStateChangeable.apply(row)) {
                continue;
            }
            table.getModel().setValueAt(!checked, row, col);
        }
    }
}
