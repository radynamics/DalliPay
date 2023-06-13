package com.radynamics.dallipay.cryptoledger;

public class WalletKey implements Key {
    private final Wallet wallet;

    public WalletKey(Wallet wallet) {
        if (wallet == null) throw new IllegalArgumentException("Parameter 'wallet' cannot be null");
        this.wallet = wallet;
    }

    @Override
    public String get() {
        return wallet.getPublicKey();
    }
}
