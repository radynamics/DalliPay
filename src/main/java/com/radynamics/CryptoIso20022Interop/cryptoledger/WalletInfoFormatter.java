package com.radynamics.CryptoIso20022Interop.cryptoledger;

public class WalletInfoFormatter {
    public static String format(WalletInfo wi) {
        if (wi == null) throw new IllegalArgumentException("Parameter 'wi' cannot be null");
        return String.format("%s (%s)", wi.getValue(), wi.getText());
    }
}
