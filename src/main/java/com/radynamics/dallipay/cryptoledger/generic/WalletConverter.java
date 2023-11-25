package com.radynamics.dallipay.cryptoledger.generic;

public final class WalletConverter {
    public static Wallet from(com.radynamics.dallipay.cryptoledger.Wallet wallet) {
        return (Wallet) wallet;
    }
}
