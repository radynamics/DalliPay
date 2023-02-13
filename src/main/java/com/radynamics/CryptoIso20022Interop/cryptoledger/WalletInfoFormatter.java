package com.radynamics.CryptoIso20022Interop.cryptoledger;

public class WalletInfoFormatter {
    public static String format(Wallet wallet, WalletInfo wi) {
        return format(wallet, wi, false);
    }

    public static String format(Wallet wallet, WalletInfo wi, boolean includePublicKey) {
        if (wi == null) {
            return wallet.getPublicKey();
        }

        var formatted = format(wi);
        return includePublicKey
                ? String.format("%s (%s)", formatted, wallet.getPublicKey())
                : formatted;
    }

    public static String format(WalletInfo wi) {
        if (wi == null) throw new IllegalArgumentException("Parameter 'wi' cannot be null");
        return String.format("%s (%s)", wi.getValue(), wi.getText());
    }
}
