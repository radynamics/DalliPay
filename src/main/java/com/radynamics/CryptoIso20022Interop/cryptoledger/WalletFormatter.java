package com.radynamics.CryptoIso20022Interop.cryptoledger;

public class WalletFormatter {
    public String format(Wallet wallet) {
        return wallet == null ? "Missing Wallet" : wallet.getPublicKey();
    }
}
