package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

public class PaymentWalletInfo {
    private Payment payment;
    private WalletInfo senderInfo;
    private WalletInfo receiverInfo;

    public PaymentWalletInfo(Payment payment, WalletInfo senderInfo, WalletInfo receiverInfo) {
        this.payment = payment;
        this.senderInfo = senderInfo;
        this.receiverInfo = receiverInfo;
    }

    public Payment getPayment() {
        return payment;
    }

    public WalletInfo getSenderInfo() {
        return senderInfo;
    }

    public WalletInfo getReceiverInfo() {
        return receiverInfo;
    }
}
