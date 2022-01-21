package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;

import java.time.LocalDateTime;

public class Payment {
    private Transaction cryptoTrx;
    private Account senderAccount;
    private Account receiverAccount;
    private Address senderAddress;
    private Address receiverAddress;
    private ExchangeRate exchangeRate;

    public Payment(Transaction cryptoTrx) {
        this.cryptoTrx = cryptoTrx;
    }

    public Address getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(Address address) {
        this.receiverAddress = address;
    }

    public Address getSenderAddress() {
        return senderAddress;
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
        if (isAmountUnknown()) {
            return null;
        }
        var amt = getLedger().convertToNativeCcyAmount(getLedgerAmountSmallestUnit());
        var cc = new CurrencyConverter(new ExchangeRate[]{exchangeRate});
        return cc.convert(amt, exchangeRate.getPair());
    }

    public String getFiatCcy() {
        if (isAmountUnknown()) {
            return "";
        }
        return exchangeRate.getPair().getFirst().equals(getLedgerCcy())
                ? exchangeRate.getPair().getSecond()
                : exchangeRate.getPair().getFirst();
    }

    public void setAmountUnknown() {
        exchangeRate = null;
    }

    public boolean isAmountUnknown() {
        return exchangeRate == null;
    }

    public void setExchangeRate(ExchangeRate rate) {
        if (!rate.getPair().affects(getLedgerCcy())) {
            throw new IllegalArgumentException(String.format("Exchange rate must affect %s.", getLedgerCcy()));
        }
        this.exchangeRate = rate;
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

    public void addStructuredReference(StructuredReference structuredReference) {
        cryptoTrx.addStructuredReference(structuredReference);
    }

    public void addMessage(String message) {
        cryptoTrx.addMessage(message);
    }

    public ExchangeRate getExchangeRate() {
        return exchangeRate;
    }
}
