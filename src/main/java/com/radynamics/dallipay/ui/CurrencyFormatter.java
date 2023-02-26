package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.Iso4217CurrencyCode;
import com.radynamics.dallipay.cryptoledger.WalletInfoProvider;
import com.radynamics.dallipay.exchange.Currency;

import javax.swing.*;
import java.util.ResourceBundle;

public class CurrencyFormatter {
    private final com.radynamics.dallipay.exchange.CurrencyFormatter currencyFormatter;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

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

        var tooltip = String.format(res.getString("issuedBy"), currencyFormatter.formatIssuer(ccy));
        if (ccy.getTransferFee() != 0) {
            tooltip += System.lineSeparator() + res.getString("transferfee") + " " + com.radynamics.dallipay.exchange.CurrencyFormatter.formatTransferFee(ccy);
        }
        lbl.setToolTipText(tooltip);

        if (Iso4217CurrencyCode.contains(ccy.getCode())) {
            lbl.setForeground(Consts.ColorIssuedFiatCcy);
        }
    }
}
