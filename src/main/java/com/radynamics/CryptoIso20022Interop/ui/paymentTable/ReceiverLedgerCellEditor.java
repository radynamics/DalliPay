package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletLookupProvider;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class ReceiverLedgerCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JComponent component;

    public ReceiverLedgerCellEditor(WalletLookupProvider lookupProvider) {
        component = new WalletField(lookupProvider);
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex, int vColIndex) {
        ((WalletField) component).setText((String) value);
        return component;
    }

    public Object getCellEditorValue() {
        return ((WalletField) component).getText();
    }
}
