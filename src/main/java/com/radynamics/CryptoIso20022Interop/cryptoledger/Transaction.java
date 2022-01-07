package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;

import java.time.LocalDateTime;

public interface Transaction {
    long getAmountSmallestUnit();

    String getCcy();

    LocalDateTime getBooked();

    void setBooked(LocalDateTime value);

    String getId();

    void setId(String value);

    Wallet getSender();

    Wallet getReceiver();

    void addStructuredReference(StructuredReference structuredReference);

    StructuredReference[] getStructuredReferences();

    void addMessage(String message);

    String[] getMessages();

    String getInvoiceId();

    void setInvoiceId(String s);

    Ledger getLedger();
}
