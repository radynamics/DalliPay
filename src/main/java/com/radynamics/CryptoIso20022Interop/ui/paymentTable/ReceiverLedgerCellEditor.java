package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletLookupProvider;
import com.radynamics.CryptoIso20022Interop.ui.WalletField;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.*;

public class ReceiverLedgerCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JComponent component;
    private final TableColumn objectColumn;

    public ReceiverLedgerCellEditor(TableColumn objectColumn, WalletLookupProvider lookupProvider) {
        this.objectColumn = objectColumn;
        this.component = new WalletField(lookupProvider);
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        var obj = (Transaction) table.getModel().getValueAt(row, objectColumn.getModelIndex());
        var field = ((WalletField) component);

        field.setLedger(obj.getLedger());
        field.setText((String) value);
        return component;
    }

    public Object getCellEditorValue() {
        return ((WalletField) component).getText();
    }
}
