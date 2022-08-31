package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.Iso4217CurrencyCode;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfoAggregator;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfoProvider;
import com.radynamics.CryptoIso20022Interop.exchange.Currency;

import javax.swing.*;

public class CurrencyFormatter {
    private final WalletInfoAggregator walletInfoAggregator;

    public CurrencyFormatter(WalletInfoProvider[] infoProviders) {
        this.walletInfoAggregator = new WalletInfoAggregator(infoProviders);
    }

    public void format(JLabel lbl, Currency ccy) {
        // Native currencies like "XRP" don't have an issuer.
        if (ccy.getIssuer() == null) {
            return;
        }

        var issuerText = ccy.getIssuer().getPublicKey();
        var wi = walletInfoAggregator == null ? null : walletInfoAggregator.getMostImportant(ccy.getIssuer());
        if (wi != null) {
            issuerText = String.format("%s (%s)", wi.getValue(), wi.getText());
        }

        lbl.setToolTipText(String.format("Issued by %s", issuerText));

        if (Iso4217CurrencyCode.contains(ccy.getCode())) {
            lbl.setForeground(Consts.ColorIssuedFiatCcy);
        }
    }
}
