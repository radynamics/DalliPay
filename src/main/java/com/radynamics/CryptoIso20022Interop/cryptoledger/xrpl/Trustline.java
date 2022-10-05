package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.exchange.Money;

public class Trustline {
    private final Wallet wallet;
    private final Money balance;
    private final Money limit;

    public Trustline(Wallet wallet, Money balance, Money limit) {
        if (wallet == null) throw new IllegalArgumentException("Parameter 'wallet' cannot be null");
        if (balance == null) throw new IllegalArgumentException("Parameter 'balance' cannot be null");
        if (limit == null) throw new IllegalArgumentException("Parameter 'limit' cannot be null");
        if (!balance.getCcy().equals(limit.getCcy())) throw new IllegalArgumentException("balance and limit must be same currency");

        this.wallet = wallet;
        this.balance = balance;
        this.limit = limit;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public Money getBalance() {
        return balance;
    }

    public Money getLimit() {
        return limit;
    }

    @Override
    public String toString() {
        return String.format("balance: %s, limit: %s", balance, limit);
    }
}
