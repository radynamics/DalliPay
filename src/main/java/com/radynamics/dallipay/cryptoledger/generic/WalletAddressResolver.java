package com.radynamics.dallipay.cryptoledger.generic;

public interface WalletAddressResolver {
    /**
     * Returns wallet address information about a given value or returns null if the input is innvalid.
     */
    WalletAddressInfo resolve(String value);
}
