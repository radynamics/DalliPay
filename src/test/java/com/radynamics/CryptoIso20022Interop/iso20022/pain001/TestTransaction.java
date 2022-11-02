package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Fee;
import com.radynamics.CryptoIso20022Interop.cryptoledger.FeeType;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class TestTransaction implements com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction {
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
