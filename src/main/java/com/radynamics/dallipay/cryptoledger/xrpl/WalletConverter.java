package com.radynamics.dallipay.cryptoledger.xrpl;

import org.xrpl.xrpl4j.model.transactions.Address;

public final class WalletConverter {
    public static Wallet from(com.radynamics.dallipay.cryptoledger.Wallet wallet) {
        return (Wallet) wallet;
    }

    public static Wallet from(Address address) {
        return new Wallet(address.value());
    }
}
