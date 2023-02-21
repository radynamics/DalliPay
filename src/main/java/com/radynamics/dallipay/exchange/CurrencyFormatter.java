package com.radynamics.dallipay.exchange;

import com.radynamics.dallipay.cryptoledger.WalletInfoAggregator;
import com.radynamics.dallipay.cryptoledger.WalletInfoFormatter;
import com.radynamics.dallipay.cryptoledger.WalletInfoProvider;

import java.text.DecimalFormat;

public class CurrencyFormatter {
    private final WalletInfoAggregator walletInfoAggregator;

    public CurrencyFormatter(WalletInfoProvider[] infoProviders) {
        this.walletInfoAggregator = new WalletInfoAggregator(infoProviders);
    }

    public String formatIssuer(Currency ccy) {
        if (ccy == null) throw new IllegalArgumentException("Parameter 'ccy' cannot be null");
        if (ccy.getIssuer() == null) {
            return "";
        }

        var issuerText = ccy.getIssuer().getPublicKey();
        var wi = walletInfoAggregator.getNameOrDomain(ccy.getIssuer());
        return wi == null ? issuerText : WalletInfoFormatter.format(wi);
    }

    public String formatCcyAndIssuer(Currency ccy) {
        if (ccy == null) throw new IllegalArgumentException("Parameter 'ccy' cannot be null");
        return ccy.getIssuer() == null ? ccy.getCode() : String.format("%s (%s)", ccy.getCode(), formatIssuer(ccy));
    }

    public static String formatTransferFee(Currency ccy) {
        if (ccy.getTransferFee() == 0) {
            return "";
        }

        var df = DecimalFormat.getInstance();
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        return df.format(ccy.getTransferFee() * 100) + "%";
    }

}
