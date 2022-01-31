package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletLookupProvider;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.ui.WalletField;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.*;

public class WalletCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final WalletField component;
    private final TableColumn objectColumn;

    public WalletCellEditor(TableColumn objectColumn, WalletLookupProvider lookupProvider, boolean editable) {
        this.objectColumn = objectColumn;
        this.component = new WalletField(lookupProvider);
        this.component.setEditable(editable);
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        var obj = (Payment) table.getModel().getValueAt(row, objectColumn.getModelIndex());
        var field = ((WalletField) component);

        field.setLedger(obj.getLedger());
        if (value instanceof WalletCellValue) {
            field.setText(((WalletCellValue) value).getWallet().getPublicKey());
        } else {
            field.setText((String) value);
        }
        return component;
    }

    public Object getCellEditorValue() {
        return component.getText();
    }
}
