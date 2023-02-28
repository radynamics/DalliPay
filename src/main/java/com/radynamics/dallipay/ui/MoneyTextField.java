package com.radynamics.dallipay.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.radynamics.dallipay.MoneyFormatter;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class MoneyTextField extends MoneyControl<JTextField> implements DocumentListener {
    private final ArrayList<ChangedListener> listener = new ArrayList<>();
    private boolean isInDocumentEventHandler;
    private boolean isInRefreshText;
    private final MoneyTextFieldInputValidator validator = new MoneyTextFieldInputValidator();
    private final ValidationControlDecorator decorator = new ValidationControlDecorator(ctrl, validator);
    private final HashMap<String, Currency> knownCurrencies = new HashMap<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    public MoneyTextField(Ledger ledger) {
        super(ledger, new JTextField());
        setEditable(false);
        ctrl.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, res.getString("moneyTextField.placeholderText"));
        ctrl.setColumns(9);
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

            // Take previously known currency (with issuer information) if present.
            var ccy = knownCurrencies.getOrDefault(m.getCcy().getCode(), null);
            if (ccy != null) {
                m = Money.of(m.getNumber(), ccy);
            }

            // Setting a rounded text in refreshText can be unequal.
            if (m.equalsIgnoringIssuer(getAmount()) || isInRefreshText) {
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
        if (Payment.isAmountUnknown(getAmount())) {
            ctrl.setText("");
            return;
        }

        try {
            isInRefreshText = true;
            var formatted = getLedger().getNativeCcySymbol().equals(value.getCcy().getCode())
                    ? MoneyFormatter.formatExact(value)
                    : MoneyFormatter.formatFiat(value);
            ctrl.setText(formatted);
        } finally {
            isInRefreshText = false;
        }
    }

    public void addChangedListener(ChangedListener l) {
        listener.add(l);
    }

    private void raiseChanged() {
        for (var l : listener) {
            l.onChanged();
        }
    }

    public void addKnownCurrency(Currency ccy) {
        knownCurrencies.put(ccy.getCode(), ccy);
    }
}
