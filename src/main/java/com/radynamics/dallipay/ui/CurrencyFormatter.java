package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.Iso4217CurrencyCode;
import com.radynamics.dallipay.cryptoledger.WalletInfoProvider;
import com.radynamics.dallipay.exchange.Currency;

import javax.swing.*;

public class CurrencyFormatter {
    private final com.radynamics.dallipay.exchange.CurrencyFormatter currencyFormatter;

    public CurrencyFormatter(WalletInfoProvider[] infoProviders) {
        this.currencyFormatter = new com.radynamics.dallipay.exchange.CurrencyFormatter(infoProviders);
    }

    public void format(JComponent lbl, Currency ccy) {
        // Native currencies like "XRP" don't have an issuer.
        if (ccy.getIssuer() == null) {
            lbl.setToolTipText("");
            lbl.setForeground(new JLabel().getForeground());
            return;
        }

        var tooltip = String.format("Issued by %s", currencyFormatter.formatIssuer(ccy));
        if (ccy.getTransferFee() != 0) {
            tooltip += System.lineSeparator() + "Transfer fee: " + com.radynamics.dallipay.exchange.CurrencyFormatter.formatTransferFee(ccy);
        }
        lbl.setToolTipText(tooltip);

        if (Iso4217CurrencyCode.contains(ccy.getCode())) {
            lbl.setForeground(Consts.ColorIssuedFiatCcy);
        }
    }
}
