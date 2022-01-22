package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import org.xrpl.xrpl4j.model.transactions.Address;

public final class WalletConverter {
    public static Wallet from(com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet wallet) {
        return (Wallet) wallet;
    }

    public static Wallet from(Address address) {
        return new Wallet(address.value());
    }
}
