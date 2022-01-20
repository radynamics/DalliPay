package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;

import java.time.LocalDateTime;

public class Payment {
    private Transaction cryptoTrx;
    private Double amount;
    private String ccy;
    private Account senderAccount;
    private Account receiverAccount;

    private static final Double UnknownAmount = Double.valueOf(0);
    private static final String UnknownCCy = "";

    public Payment(Transaction cryptoTrx) {
        this.cryptoTrx = cryptoTrx;
    }

    public Address getReceiverAddress() {
        return cryptoTrx.getReceiverAddress();
    }

    public Address getSenderAddress() {
        return cryptoTrx.getSenderAddress();
    }

    public Account getReceiverAccount() {
        return receiverAccount;
    }

    public void setReceiverAccount(Account account) {
        receiverAccount = account;
    }

    public Account getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(Account account) {
        senderAccount = account;
    }

    public LocalDateTime getBooked() {
        return cryptoTrx.getBooked();
    }

    public TransmissionState getTransmission() {
        return cryptoTrx.getTransmission();
    }

    public Wallet getReceiverWallet() {
        return cryptoTrx.getReceiverWallet();
    }

    public Wallet getSenderWallet() {
        return cryptoTrx.getSenderWallet();
    }

    public Double getAmount() {
        return amount;
    }

    public String getFiatCcy() {
        return this.ccy;
    }

    public void setAmountUnknown() {
        setAmount(UnknownAmount, UnknownCCy);
    }

    public boolean isAmountUnknown() {
        return UnknownAmount.equals(amount) && UnknownCCy.equals(ccy);
    }

    public void setAmount(double value, String ccy) {
        this.amount = value;
        this.ccy = ccy;
    }

    public long getLedgerAmountSmallestUnit() {
        return cryptoTrx.getAmountSmallestUnit();
    }

    public String getId() {
        return cryptoTrx.getId();
    }

    public StructuredReference[] getStructuredReferences() {
        return cryptoTrx.getStructuredReferences();
    }

    public String getInvoiceId() {
        return cryptoTrx.getInvoiceId();
    }

    public String[] getMessages() {
        return cryptoTrx.getMessages();
    }

    public void setReceiverWallet(Wallet wallet) {
        cryptoTrx.setReceiverWallet(wallet);
    }

    public Transaction getTransaction() {
        return cryptoTrx;
    }

    public String getLedgerCcy() {
        return cryptoTrx.getCcy();
    }

    public Ledger getLedger() {
        return cryptoTrx.getLedger();
    }
}
