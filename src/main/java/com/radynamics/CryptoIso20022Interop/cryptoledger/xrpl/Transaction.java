package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Transaction implements com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction {

    private String id;
    private Ledger ledger;
    private long drops;
    private String ccy;
    private LocalDateTime booked;
    private com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet senderWallet;
    private com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet receiverWallet;
    private Account senderAccount;
    private Account receiverAccount;
    private ArrayList<String> messages = new ArrayList<>();
    private ArrayList<StructuredReference> references = new ArrayList<>();
    private String invoiceId;

    public Transaction(Ledger ledger, long drops, String ccy) {
        this.ledger = ledger;
        this.drops = drops;
        this.ccy = ccy;
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    @Override
    public long getAmountSmallestUnit() {
        return drops;
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
        this.id = value;
    }

    @Override
    public void setSender(Account account) {
        senderAccount = account;
    }

    @Override
    public Wallet getSenderWallet() {
        return senderWallet;
    }

    @Override
    public void setReceiver(Account account) {
        receiverAccount = account;
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
    public void addStructuredReference(StructuredReference value) {
        references.add(value);
    }

    @Override
    public StructuredReference[] getStructuredReferences() {
        return references.toArray(new StructuredReference[0]);
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

    public void setSender(com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet sender) {
        this.senderWallet = sender;
    }

    public void setReceiver(com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet receiver) {
        this.receiverWallet = receiver;
    }

    public void setAmount(long drops) {
        this.drops = drops;
    }
}
