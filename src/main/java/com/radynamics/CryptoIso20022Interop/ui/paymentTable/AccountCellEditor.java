package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.OtherAccount;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class AccountCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JTextField component;

    public AccountCellEditor(boolean editable) {
        this.component = new JTextField();
        this.component.setEditable(editable);
        this.component.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                var text = ((JTextField) input).getText();
                if (text.length() > 0) {
                    component.putClientProperty("JComponent.outline", null);
                    return true;
                } else {
                    component.putClientProperty("JComponent.outline", "error");
                    return false;
                }
            }
        });
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        var field = ((JTextField) component);

        if (value instanceof IbanAccount) {
            var iban = (IbanAccount) value;
            field.setText(iban.getFormatted());
        } else if (value instanceof OtherAccount) {
            var iban = (OtherAccount) value;
            field.setText(iban.getUnformatted());
        } else {
            field.setText(value.toString());
        }
        component.getInputVerifier().verify(component);
        return component;
    }

    public Object getCellEditorValue() {
        return component.getText();
    }

}
