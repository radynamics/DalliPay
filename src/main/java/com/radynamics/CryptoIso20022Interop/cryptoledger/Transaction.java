package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;

import java.time.LocalDateTime;

public interface Transaction {
    void setAmountSmallestUnit(long value);

    long getAmountSmallestUnit();

    String getCcy();

    LocalDateTime getBooked();

    void setBooked(LocalDateTime value);

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
}
