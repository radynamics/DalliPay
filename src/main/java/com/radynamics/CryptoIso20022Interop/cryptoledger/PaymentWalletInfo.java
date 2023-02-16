package com.radynamics.CryptoIso20022Interop.cryptoledger;

public class PaymentWalletInfo {
    private final WalletInfo[] senderInfos;
    private final WalletInfo[] receiverInfos;

    public PaymentWalletInfo(WalletInfo[] senderInfos, WalletInfo[] receiverInfos) {
        this.senderInfos = senderInfos;
        this.receiverInfos = receiverInfos;
    }

    public WalletInfo[] getSenderInfos() {
        return senderInfos;
    }

    public WalletInfo[] getReceiverInfos() {
        return receiverInfos;
    }
}
