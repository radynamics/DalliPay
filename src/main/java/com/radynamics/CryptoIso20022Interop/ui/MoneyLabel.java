package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.AmountFormatter;

import javax.swing.*;

public class MoneyLabel extends MoneyControl<JLabel> {
    public MoneyLabel(Ledger ledger) {
        super(ledger, new JLabel());
    }

    public void formatAsSecondaryInfo() {
        Utils.formatSecondaryInfo(ctrl);
    }

    @Override
    protected void refreshText(Money value) {
        ctrl.setText(AmountFormatter.formatAmtWithCcy(value));
    }
}
