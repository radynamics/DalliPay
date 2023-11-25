package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.generic.Wallet;

public final class WalletConverter {
    public static Wallet from(com.radynamics.dallipay.cryptoledger.Wallet wallet) {
        return (Wallet) wallet;
    }
}
