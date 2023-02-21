package com.radynamics.dallipay.cryptoledger;

public interface Wallet {
    String getPublicKey();

    String getSecret();

    void setSecret(String secret);

    MoneyBag getBalances();

    LedgerId getLedgerId();
}
