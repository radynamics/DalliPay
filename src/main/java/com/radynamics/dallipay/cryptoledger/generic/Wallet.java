package com.radynamics.dallipay.cryptoledger.generic;

import com.radynamics.dallipay.cryptoledger.LedgerId;
import com.radynamics.dallipay.cryptoledger.MoneyBag;

public class Wallet implements com.radynamics.dallipay.cryptoledger.Wallet {
    private final LedgerId ledgerId;
    private String publicKey;
    private String secret;
    private MoneyBag balances = new MoneyBag();

    public Wallet(LedgerId ledgerId, String publicKey) {
        this(ledgerId, publicKey, null);
    }

    public Wallet(LedgerId ledgerId, String publicKey, String secret) {
        this.ledgerId = ledgerId;
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
        return ledgerId;
    }

    @Override
    public String toString() {
        return String.format("%s, %s", publicKey, secret);
    }
}
