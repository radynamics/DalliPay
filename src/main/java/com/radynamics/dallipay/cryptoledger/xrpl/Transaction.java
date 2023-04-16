package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.Fee;
import com.radynamics.dallipay.cryptoledger.FeeType;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.transaction.TransmissionState;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReference;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Transaction implements com.radynamics.dallipay.cryptoledger.Transaction {
    private String id;
    private Ledger ledger;
    private Money amt;
    private ZonedDateTime booked;
    private com.radynamics.dallipay.cryptoledger.Wallet senderWallet;
    private com.radynamics.dallipay.cryptoledger.Wallet receiverWallet;
    private String destinationTag;
    private ArrayList<String> messages = new ArrayList<>();
    private ArrayList<StructuredReference> references = new ArrayList<>();
    private String invoiceId;
    private TransmissionState transmission = TransmissionState.Pending;
    private Throwable transmissionError;
    private Money fee;

    public Transaction(Ledger ledger, Money amt) {
        if (amt == null) throw new IllegalArgumentException("Parameter 'amt' cannot be null");
        this.ledger = ledger;
        this.amt = amt;
        this.fee = Money.zero(new Currency(ledger.getNativeCcySymbol()));
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
    public void setId(String value) {
        this.id = value;
    }

    @Override
    public Wallet getSenderWallet() {
        return senderWallet;
    }

    @Override
    public void setSenderWallet(Wallet wallet) {
        this.senderWallet = WalletConverter.from(wallet);
    }

    @Override
    public Wallet getReceiverWallet() {
        return receiverWallet;
    }

    @Override
    public void setReceiverWallet(Wallet wallet) {
        setReceiver(WalletConverter.from(wallet));
    }

    @Override
    public String getDestinationTag() {
        return destinationTag;
    }

    @Override
    public void setDestinationTag(String destinationTag) {
        this.destinationTag = destinationTag;
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
        return transmission;
    }

    @Override
    public Throwable getTransmissionError() {
        return transmissionError;
    }

    @Override
    public Fee[] getFees() {
        var list = new ArrayList<Fee>();
        list.add(new Fee(fee, FeeType.LedgerTransactionFee));
        var transferFee = getAmount().getCcy().getTransferFee();
        if (transferFee != 0) {
            list.add(new Fee(getAmount().multiply(transferFee), FeeType.CurrencyTransferFee));
        }
        return list.toArray(new Fee[0]);
    }

    @Override
    public void setLedgerTransactionFee(Money value) {
        if (!value.getCcy().getCode().equals(ledger.getNativeCcySymbol())) {
            throw new IllegalArgumentException(String.format("Currency of fee must be %s", ledger.getNativeCcySymbol()));
        }
        fee = value;
    }

    public void setSender(com.radynamics.dallipay.cryptoledger.Wallet sender) {
        this.senderWallet = sender;
    }

    public void setReceiver(com.radynamics.dallipay.cryptoledger.Wallet receiver) {
        this.receiverWallet = receiver;
    }

    public void refreshTransmission() {
        this.transmissionError = null;
        refreshTransmissionState(StringUtils.isAllEmpty(getId()) ? TransmissionState.Error : TransmissionState.Success);
    }

    public void refreshTransmission(Throwable t) {
        this.transmissionError = t;
        refreshTransmissionState(TransmissionState.Error);
    }

    public void refreshTransmissionState(TransmissionState state) {
        this.transmission = state;
    }
}
