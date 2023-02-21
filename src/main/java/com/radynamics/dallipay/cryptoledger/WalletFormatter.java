package com.radynamics.dallipay.cryptoledger;

public class WalletFormatter {
    public String format(Wallet wallet) {
        return wallet == null ? "Missing Wallet" : wallet.getPublicKey();
    }
}
