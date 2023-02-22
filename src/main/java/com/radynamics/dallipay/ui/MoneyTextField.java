package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.MoneyFormatter;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;

public class MoneyTextField extends MoneyControl<JTextField> implements DocumentListener {
    private final ArrayList<ChangedListener> listener = new ArrayList<>();
    private boolean isInDocumentEventHandler;
    private final MoneyTextFieldInputValidator validator = new MoneyTextFieldInputValidator();
    private final ValidationControlDecorator decorator = new ValidationControlDecorator(ctrl, validator);

    public MoneyTextField(Ledger ledger) {
        super(ledger, new JTextField());
        setEditable(false);
        ctrl.getDocument().addDocumentListener(this);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        onTextChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        onTextChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        onTextChanged();
    }

    private void onTextChanged() {
        try {
            isInDocumentEventHandler = true;

            var value = ctrl.getText();
            decorator.update(value);
            var m = validator.getValidOrNull(value);
            if (m == null) {
                return;
            }

            if (m.equalsIgnoringIssuer(getAmount())) {
                return;
            }

            setAmount(m);
            raiseChanged();
        } finally {
            isInDocumentEventHandler = false;
        }
    }

    public void setEditable(boolean editable) {
        ctrl.setEditable(editable);
        ctrl.setFocusable(editable);
    }

    @Override
    protected void refreshText(Money value) {
        if (isInDocumentEventHandler) {
            return;
        }
        ctrl.setText(Payment.isAmountUnknown(getAmount()) ? "" : MoneyFormatter.formatExact(value));
    }

    public void addChangedListener(ChangedListener l) {
        listener.add(l);
    }

    private void raiseChanged() {
        for (var l : listener) {
            l.onChanged();
        }
    }
}