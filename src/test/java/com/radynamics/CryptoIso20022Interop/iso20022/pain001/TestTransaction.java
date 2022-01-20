package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TestTransaction implements com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction {
    private String id;
    private long amountSmallestUnit;
    private String ccy;
    private LocalDateTime booked;
    private Wallet senderWallet;
    private Wallet receiverWallet;
    private Account senderAccount;
    private Account receiverAccount;
    private ArrayList<String> messages = new ArrayList<>();
    private ArrayList<StructuredReference> references = new ArrayList<>();
    private String invoiceId;
    private Ledger ledger;

    public TestTransaction(Ledger ledger, long amountSmallestUnit, String ccy) {
        this.ledger = ledger;
        this.amountSmallestUnit = amountSmallestUnit;
        this.ccy = ccy;
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    @Override
    public long getAmountSmallestUnit() {
        return amountSmallestUnit;
    }

    @Override
    public String getCcy() {
        return ccy;
    }

    @Override
    public LocalDateTime getBooked() {
        return booked;
    }

    @Override
    public void setBooked(LocalDateTime value) {
        this.booked = value;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String value) {
        this.id = id;
    }

    @Override
    public Account getSenderAccount() {
        return senderAccount;
    }

    @Override
    public void setSenderAccount(Account account) {
        this.senderAccount = account;
    }

    @Override
    public Wallet getSenderWallet() {
        return senderWallet;
    }

    @Override
    public void setReceiverAccount(Account account) {
        this.receiverAccount = account;
    }

    @Override
    public Account getReceiverAccount() {
        return receiverAccount;
    }

    @Override
    public Wallet getReceiverWallet() {
        return receiverWallet;
    }

    @Override
    public void setReceiverWallet(Wallet wallet) {
        setReceiver(wallet);
    }

    @Override
    public void addStructuredReference(StructuredReference value) {
        references.add(value);
    }

    @Override
    public StructuredReference[] getStructuredReferences() {
        return references.toArray(new StructuredReference[0]);
    }

    @Override
    public void removeStructuredReferences(int index) {
        references.remove(index);
    }

    @Override
    public String[] getMessages() {
        return messages.toArray(new String[0]);
    }

    @Override
    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    @Override
    public Ledger getLedger() {
        return ledger;
    }

    @Override
    public TransmissionState getTransmission() {
        return TransmissionState.Pending;
    }

    public void setSender(Wallet sender) {
        this.senderWallet = sender;
    }

    public void setReceiver(Wallet receiver) {
        this.receiverWallet = receiver;
    }

    public void setAmount(long drops) {
        this.amountSmallestUnit = drops;
    }
}
