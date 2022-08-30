package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;

import java.time.ZonedDateTime;

public interface Transaction {
    void setAmount(Double value);

    Double getAmount();

    Currency getCcy();

    ZonedDateTime getBooked();

    void setBooked(ZonedDateTime value);

    String getId();

    void setId(String value);

    Wallet getSenderWallet();

    void setSenderWallet(Wallet wallet);

    Wallet getReceiverWallet();

    void setReceiverWallet(Wallet wallet);

    void addStructuredReference(StructuredReference structuredReference);

    StructuredReference[] getStructuredReferences();

    void removeStructuredReferences(int index);

    void addMessage(String message);

    String[] getMessages();

    String getInvoiceId();

    void setInvoiceId(String s);

    Ledger getLedger();

    TransmissionState getTransmission();

    Throwable getTransmissionError();

    Money getFee();

    void setFee(Money value);
}
