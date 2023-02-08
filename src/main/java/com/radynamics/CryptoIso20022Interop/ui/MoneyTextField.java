package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

public class MoneyTextField extends MoneyControl<JTextField> implements DocumentListener {
    private final static Logger log = LogManager.getLogger(MoneyTextField.class);
    private final ArrayList<ChangedListener> listener = new ArrayList<>();
    private boolean isInDocumentEventHandler;

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

            // Eg: "10.50 USD"
            var words = ctrl.getText().split(" ");
            for (var i = 0; i < words.length; i++) {
                words[i] = StringUtils.deleteWhitespace(words[i]);
            }
            if (words.length != 2 || words[0].length() == 0 || words[1].length() == 0) {
                return;
            }

            var nf = NumberFormat.getInstance(Locale.getDefault());
            // Eg: "1'000.50 USD"
            var amt = nf.parse(words[0]).doubleValue();
            var ccy = new Currency(words[1].toUpperCase());

            var m = Money.of(amt, ccy);
            if (m.equalsIgnoringIssuer(getAmount())) {
                return;
            }

            setAmount(m);
            raiseChanged();
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        } finally {
            isInDocumentEventHandler = false;
        }
    }

    public void setEditable(boolean editable) {
        ctrl.setEditable(editable);
        ctrl.setFocusable(editable);
    }

    @Override
    protected void setText(String text) {
        if (isInDocumentEventHandler) {
            return;
        }
        ctrl.setText(Payment.isAmountUnknown(getAmount()) ? "" : text);
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
