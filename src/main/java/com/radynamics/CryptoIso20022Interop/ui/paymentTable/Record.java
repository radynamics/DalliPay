package com.radynamics.CryptoIso20022Interop.ui.paymentTable;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

public class Record {
    public Payment payment;
    public ValidationResult[] validationResults;
    public boolean selected;
    public ValidationState status;

    private Object senderLedger;
    private Object receiverLedger;
    private Double amount;

    public Record(Payment p) {
        payment = p;
        senderLedger = new WalletCellValue(payment.getSenderWallet());
        receiverLedger = new WalletCellValue(payment.getReceiverWallet());
    }

    public Object getSenderLedger() {
        return senderLedger;
    }

    public void setSenderLedger(WalletCellValue value) {
        senderLedger = value;
        payment.setSenderWallet(value.getWallet());
    }

    public void setSenderLedger(String value) {
        senderLedger = value;
    }

    public Object getReceiverLedger() {
        return receiverLedger;
    }

    public void setReceiverLedger(WalletCellValue value) {
        receiverLedger = value;
        payment.setReceiverWallet(value.getWallet());
    }

    public void setReceiverLedger(String value) {
        receiverLedger = value;
    }

    public Object getActorAddressOrAccount(Actor actor) {
        Object actorAddressOrAccount = actor.get(payment.getSenderAddress(), payment.getReceiverAddress());
        if (actorAddressOrAccount == null) {
            var actorAccount = actor.get(payment.getSenderAccount(), payment.getReceiverAccount());
            actorAddressOrAccount = actorAccount == null ? IbanAccount.Empty : actorAccount;
        }
        return actorAddressOrAccount;
    }

    public Double getAmount(Actor actor) {
        return actor == Actor.Sender ? payment.getAmount() : amount;
    }

    public void setAmount(Double value) {
        amount = value;
    }

    @Override
    public String toString() {
        return payment.toString();
    }
}
