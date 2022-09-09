package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfoProvider;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.AmountFormatter;

import javax.swing.*;
import java.awt.*;

public class MoneyLabel extends JPanel {
    private final JLabel amt = new JLabel();
    private Money value;
    private final WalletInfoProvider[] infoProviders;

    public MoneyLabel(WalletInfoProvider[] infoProviders) {
        if (infoProviders == null) throw new IllegalArgumentException("Parameter 'infoProviders' cannot be null");
        this.infoProviders = infoProviders;
        setupUI();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        {
            var c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 0.5;
            c.gridx = 0;
            c.gridy = 0;
            add(amt, c);
        }
    }

    public void setAmount(Money value) {
        this.value = value;
        format();
    }

    private void format() {
        if (value == null) {
            amt.setText("");
        }
        amt.setText(AmountFormatter.formatAmtWithCcy(value));
        var ccyFormatter = new CurrencyFormatter(infoProviders);
        ccyFormatter.format(amt, value.getCcy());
    }
}
