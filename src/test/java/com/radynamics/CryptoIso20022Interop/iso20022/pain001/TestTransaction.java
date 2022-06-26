package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class TestTransaction implements com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction {
    private String id;
    private Double amt;
    private String ccy;
    private ZonedDateTime booked;
    private Wallet senderWallet;
    private Wallet receiverWallet;
    private ArrayList<String> messages = new ArrayList<>();
    private ArrayList<StructuredReference> references = new ArrayList<>();
    private String invoiceId;
    private Ledger ledger;
    private long fee = 10;

    public TestTransaction(Ledger ledger, Double amt, String ccy) {
        if (amt == null) throw new IllegalArgumentException("Parameter 'amt' cannot be null");
        this.ledger = ledger;
        this.amt = amt;
        this.ccy = ccy;
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    @Override
    public void setAmountLedgerUnit(Double value) {
        amt = value;
    }

    @Override
    public Double getAmountLedgerUnit() {
        return amt;
    }

    @Override
    public String getCcy() {
        return ccy;
    }

    @Override
    public ZonedDateTime getBooked() {
        return booked;
    }

    @Override
    public void setBooked(ZonedDateTime value) {
        this.booked = value;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Wallet getSenderWallet() {
        return senderWallet;
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

    @Override
    public Throwable getTransmissionError() {
        return null;
    }

    @Override
    public long getFeeSmallestUnit() {
        return fee;
    }

    @Override
    public void setFeeSmallestUnit(long value) {
        fee = value;
    }

    public void setSenderWallet(Wallet wallet) {
        this.senderWallet = wallet;
    }

    public void setReceiver(Wallet receiver) {
        this.receiverWallet = receiver;
    }
}
