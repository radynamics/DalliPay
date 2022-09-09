package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.MoneyFormatter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.exchange.*;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class Payment {
    private Transaction cryptoTrx;
    private Account senderAccount;
    private Account receiverAccount;
    private Address senderAddress;
    private Address receiverAddress;
    private Double amount = UnknownAmount;
    private Currency ccy = UnknownCCy;
    private ExchangeRate exchangeRate;

    private static final Double UnknownAmount = Double.valueOf(0);
    private static final Currency UnknownCCy = null;
    private boolean amountDefined;

    public Payment(Transaction cryptoTrx) {
        if (cryptoTrx == null) throw new IllegalArgumentException("Parameter 'cryptoTrx' cannot be null");
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

    public void setSenderAddress(Address senderAddress) {
        this.senderAddress = senderAddress;
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

    public ZonedDateTime getBooked() {
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
        return this.amount;
    }

    public void setAmount(Money amt) {
        if (amt == null) throw new IllegalArgumentException("Parameter 'amt' cannot be null");
        this.amount = amt.getNumber().doubleValue();
        this.ccy = amt.getCcy();
        if (!UnknownAmount.equals(amt.getNumber())) {
            this.amountDefined = true;
        }

        refreshTransactionAmount();
    }

    private void refreshTransactionAmount() {
        if (exchangeRate == null) {
            cryptoTrx.setAmount(Money.zero(cryptoTrx.getAmount()));
        } else {
            var cc = new CurrencyConverter(new ExchangeRate[]{exchangeRate});
            var amt = cc.convert(BigDecimal.valueOf(amount), exchangeRate.getPair().invert());
            cryptoTrx.setAmount(Money.of(amt, cryptoTrx.getAmount().getCcy()));
        }
    }

    private void refreshAmount() {
        if (exchangeRate == null) {
            this.amount = UnknownAmount;
            return;
        }

        // Ccy read from pain.001 without exchange rates doesn't need a calc.
        if (!this.amount.equals(UnknownAmount) && exchangeRate.isNone()) {
            return;
        }

        var amt = BigDecimal.valueOf(getAmountTransaction().getNumber().doubleValue());
        var cc = new CurrencyConverter(new ExchangeRate[]{exchangeRate});
        this.amount = cc.convert(amt, exchangeRate.getPair());
        if (isCcyUnknown()) {
            this.ccy = exchangeRate.getPair().getFirst().equals(getAmountTransaction().getCcy())
                    ? exchangeRate.getPair().getSecond()
                    : exchangeRate.getPair().getFirst();
        }
    }

    public String getFiatCcy() {
        return this.ccy == null ? "" : this.ccy.getCode();
    }

    // TODO: remove getFiatCcy
    public Currency getUserCcy() {
        return this.ccy;
    }

    public void setUserCcy(Currency ccy) {
        this.ccy = ccy;
    }

    public void setAmountUnknown() {
        amount = UnknownAmount;
    }

    public boolean isAmountUnknown() {
        return amount == UnknownAmount;
    }

    public boolean isCcyUnknown() {
        return UnknownCCy == ccy;
    }

    public void setExchangeRate(ExchangeRate rate) {
        var bothCcyKnown = !isAmountUnknown() && !isCcyUnknown();
        if (rate != null) {
            var affectsFiat = rate.getPair().affects(getFiatCcy());
            var affectsLedger = rate.getPair().affects(getAmountTransaction().getCcy().getCode());
            if (!affectsLedger) {
                throw new IllegalArgumentException(String.format("Exchange rate must affect %s", getAmountTransaction().getCcy().getCode()));
            }
            if (bothCcyKnown && !affectsFiat) {
                throw new IllegalArgumentException(String.format("Exchange rate must affect %s and %s.", getFiatCcy(), getAmountTransaction().getCcy().getCode()));
            }
        }
        this.exchangeRate = rate;
        refreshAmounts();
    }

    public void refreshAmounts() {
        if (amountDefined) {
            refreshTransactionAmount();
        } else {
            refreshAmount();
        }
    }

    public Money getAmountTransaction() {
        return cryptoTrx.getAmount();
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

    public void setSenderWallet(Wallet wallet) {
        cryptoTrx.setSenderWallet(wallet);
    }

    public void setReceiverWallet(Wallet wallet) {
        cryptoTrx.setReceiverWallet(wallet);
    }

    public Transaction getTransaction() {
        return cryptoTrx;
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

    public Money getFee() {
        return cryptoTrx.getFee();
    }

    public void setFee(Money value) {
        cryptoTrx.setFee(value);
    }

    public String getDisplayText() {
        var amount = getAmount();
        if (amount != null) {
            return MoneyFormatter.formatFiat(BigDecimal.valueOf(amount), getFiatCcy());
        }

        return MoneyFormatter.formatLedger(cryptoTrx.getAmount());
    }

    public Throwable getTransmissionError() {
        return cryptoTrx.getTransmissionError();
    }

    public CurrencyPair createCcyPair() {
        return new CurrencyPair(getAmountTransaction().getCcy(), getUserCcy());
    }
}
