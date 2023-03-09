package com.radynamics.dallipay.iso20022.pain001;

import com.google.common.primitives.UnsignedInteger;
import com.radynamics.dallipay.cryptoledger.Fee;
import com.radynamics.dallipay.cryptoledger.FeeType;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.transaction.TransmissionState;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReference;
import jakarta.ws.rs.NotSupportedException;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestTransaction implements com.radynamics.dallipay.cryptoledger.Transaction {
    private String id;
    private Money amt;
    private ZonedDateTime booked;
    private Wallet senderWallet;
    private Wallet receiverWallet;
    private ArrayList<String> messages = new ArrayList<>();
    private ArrayList<StructuredReference> references = new ArrayList<>();
    private String invoiceId;
    private Ledger ledger;
    private Money fee;

    public TestTransaction(Ledger ledger, Double amt, String ccy) {
        this(ledger, Money.of(amt, new Currency(ccy)));
    }

    public TestTransaction(Ledger ledger, Money amt) {
        if (amt == null) throw new IllegalArgumentException("Parameter 'amt' cannot be null");
        this.ledger = ledger;
        this.amt = amt;
        this.fee = TestLedger.convertToNativeCcyAmount(10);
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    @Override
    public void setAmount(Money value) {
        amt = value;
    }

    @Override
    public Money getAmount() {
        return amt;
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
    public UnsignedInteger getDestinationTag() {
        return null;
    }

    @Override
    public void setDestinationTag(UnsignedInteger destinationTag) {
        throw new NotSupportedException();
    }

    @Override
    public void addStructuredReference(StructuredReference value) {
        references.add(value);
    }

    @Override
    public void setStructuredReference(StructuredReference[] structuredReferences) {
        references.clear();
        references.addAll(List.of(structuredReferences));
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
    public void setMessage(String[] messages) {
        this.messages.clear();
        this.messages.addAll(List.of(messages));
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
    public Fee[] getFees() {
        return new Fee[]{new Fee(fee, FeeType.LedgerTransactionFee)};
    }

    @Override
    public void setLedgerTransactionFee(Money value) {
        fee = value;
    }

    public void setSenderWallet(Wallet wallet) {
        this.senderWallet = wallet;
    }

    public void setReceiver(Wallet receiver) {
        this.receiverWallet = receiver;
    }
}
