package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.Iso4217CurrencyCode;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfoProvider;
import com.radynamics.CryptoIso20022Interop.exchange.Currency;

import javax.swing.*;

public class CurrencyFormatter {
    private final com.radynamics.CryptoIso20022Interop.exchange.CurrencyFormatter currencyFormatter;

    public CurrencyFormatter(WalletInfoProvider[] infoProviders) {
        this.currencyFormatter = new com.radynamics.CryptoIso20022Interop.exchange.CurrencyFormatter(infoProviders);
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
            tooltip += "\nTransfer fee: " + com.radynamics.CryptoIso20022Interop.exchange.CurrencyFormatter.formatTransferFee(ccy);
        }
        lbl.setToolTipText(tooltip);

        if (Iso4217CurrencyCode.contains(ccy.getCode())) {
            lbl.setForeground(Consts.ColorIssuedFiatCcy);
        }
    }
}
