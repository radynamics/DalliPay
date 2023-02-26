package com.radynamics.dallipay.cryptoledger;

import java.util.ResourceBundle;

public class WalletFormatter {
    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    public String format(Wallet wallet) {
        return wallet == null ? res.getString("missingWallet") : wallet.getPublicKey();
    }
}
