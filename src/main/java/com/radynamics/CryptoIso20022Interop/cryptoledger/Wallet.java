package com.radynamics.CryptoIso20022Interop.cryptoledger;

public interface Wallet {
    String getPublicKey();

    String getSecret();

    void setSecret(String secret);

    MoneyBag getBalances();

    LedgerId getLedgerId();
}
