package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;

import java.time.ZonedDateTime;

public interface Transaction {
    void setAmount(Money value);

    Money getAmount();

    ZonedDateTime getBooked();

    void setBooked(ZonedDateTime value);

    String getId();

    void setId(String value);

    Wallet getSenderWallet();

    void setSenderWallet(Wallet wallet);

    Wallet getReceiverWallet();

    void setReceiverWallet(Wallet wallet);

    void addStructuredReference(StructuredReference structuredReference);

    void setStructuredReference(StructuredReference[] structuredReferences);

    StructuredReference[] getStructuredReferences();

    void removeStructuredReferences(int index);

    void addMessage(String message);

    String[] getMessages();

    String getInvoiceId();

    void setInvoiceId(String s);

    Ledger getLedger();

    TransmissionState getTransmission();

    Throwable getTransmissionError();

    Fee[] getFees();

    void setLedgerTransactionFee(Money value);
}
