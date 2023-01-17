package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Fee;
import com.radynamics.CryptoIso20022Interop.cryptoledger.FeeType;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class Transaction implements com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction {
    private String id;
    private Ledger ledger;
    private Money amt;
    private ZonedDateTime booked;
    private com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet senderWallet;
    private com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet receiverWallet;
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

    public void setSender(com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet sender) {
        this.senderWallet = sender;
    }

    public void setReceiver(com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet receiver) {
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
