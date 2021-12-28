package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TestTransaction implements com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction {
    private String id;
    private long amountSmallestUnit;
    private String ccy;
    private LocalDateTime booked;
    private Wallet sender;
    private Wallet receiver;
    private ArrayList<String> messages = new ArrayList<>();
    private ArrayList<StructuredReference> references = new ArrayList<>();
    private String invoiceId;

    public TestTransaction(long amountSmallestUnit, String ccy) {
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
    public Wallet getSender() {
        return sender;
    }

    @Override
    public Wallet getReceiver() {
        return receiver;
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

    public void setSender(Wallet sender) {
        this.sender = sender;
    }

    public void setReceiver(Wallet receiver) {
        this.receiver = receiver;
    }

    public void setAmount(long drops) {
        this.amountSmallestUnit = drops;
    }
}
