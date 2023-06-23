package com.radynamics.dallipay.ui.paymentTable;

import com.radynamics.dallipay.iso20022.Account;
import com.radynamics.dallipay.ui.AccountField;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class AccountCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final AccountField component;

    public AccountCellEditor(JComponent owner, boolean editable) {
        this.component = new AccountField(owner);
        this.component.setEditable(editable);
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        var field = ((AccountField) component);
        field.setAccount((Account) value);
        return component;
    }

    public Object getCellEditorValue() {
        return component.getText();
    }

}
