package com.radynamics.dallipay.cryptoledger.generic;

import com.radynamics.dallipay.cryptoledger.Wallet;

public class WalletAddressInfo {
    private final Wallet wallet;
    private String destinationTag;

    public WalletAddressInfo(Wallet wallet) {
        this.wallet = wallet;
    }

    public com.radynamics.dallipay.cryptoledger.Wallet getWallet() {
        return wallet;
    }

    public String getDestinationTag() {
        return destinationTag;
    }

    public void setDestinationTag(String destinationTag) {
        this.destinationTag = destinationTag;
    }
}
