package com.radynamics.dallipay.ui.paymentTable;

import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.ui.WalletField;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.*;

public class WalletCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final WalletField component;
    private final TableColumn objectColumn;

    public WalletCellEditor(JComponent owner, TableColumn objectColumn, boolean editable) {
        this.objectColumn = objectColumn;
        this.component = new WalletField(owner);
        this.component.setEditable(editable);
        this.component.setShowDetailVisible(true);
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        var obj = (Payment) table.getModel().getValueAt(row, objectColumn.getModelIndex());
        var field = ((WalletField) component);

        field.setLedger(obj.getLedger());
        if (value instanceof WalletCellValue) {
            var wallet = ((WalletCellValue) value).getWallet();
            field.setText(wallet == null ? "" : wallet.getPublicKey());
        } else {
            field.setText((String) value);
        }
        return component;
    }

    public Object getCellEditorValue() {
        if (component.getText().length() == 0) {
            return "";
        }

        var addressInfo = component.getLedger().createWalletAddressResolver().resolve(component.getText());
        return addressInfo == null
                // User input is invalid
                ? new WalletCellValue(component.getLedger().createWallet(component.getText(), null), component.getDestinationTag())
                // User input is recognized
                : new WalletCellValue(addressInfo.getWallet(), addressInfo.getDestinationTag());
    }
}
