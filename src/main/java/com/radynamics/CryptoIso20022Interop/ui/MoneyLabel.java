package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;

import javax.swing.*;

public class MoneyLabel extends MoneyControl<JLabel> {
    public MoneyLabel(Ledger ledger) {
        super(ledger, new JLabel());
    }

    public void formatAsSecondaryInfo() {
        Utils.formatSecondaryInfo(ctrl);
    }

    @Override
    protected void setText(String text) {
        ctrl.setText(text);
    }
}
