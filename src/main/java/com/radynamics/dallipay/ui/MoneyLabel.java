package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.AmountFormatter;

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
