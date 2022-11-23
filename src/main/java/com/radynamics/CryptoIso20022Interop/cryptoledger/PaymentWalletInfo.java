package com.radynamics.CryptoIso20022Interop.cryptoledger;

public class PaymentWalletInfo {
    private WalletInfo senderInfo;
    private WalletInfo receiverInfo;

    public PaymentWalletInfo(WalletInfo senderInfo, WalletInfo receiverInfo) {
        this.senderInfo = senderInfo;
        this.receiverInfo = receiverInfo;
    }

    public WalletInfo getSenderInfo() {
        return senderInfo;
    }

    public WalletInfo getReceiverInfo() {
        return receiverInfo;
    }
}
