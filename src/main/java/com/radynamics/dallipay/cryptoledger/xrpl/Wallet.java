package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.MoneyBag;
import com.radynamics.dallipay.cryptoledger.LedgerId;

public class Wallet implements com.radynamics.dallipay.cryptoledger.Wallet {

    private String publicKey;
    private String secret;
    private MoneyBag balances = new MoneyBag();

    public Wallet(String publicKey) {
        this(publicKey, null);
    }

    public Wallet(String publicKey, String secret) {
        this.publicKey = publicKey;
        this.secret = secret;
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    @Override
    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public MoneyBag getBalances() {
        return balances;
    }

    @Override
    public LedgerId getLedgerId() {
        return LedgerId.Xrpl;
    }

    @Override
    public String toString() {
        return String.format("%s, %s", publicKey, secret);
    }
}
