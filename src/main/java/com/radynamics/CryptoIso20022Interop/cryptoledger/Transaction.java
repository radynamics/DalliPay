package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.TransmissionState;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
import com.radynamics.CryptoIso20022Interop.iso20022.Address;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;

import java.time.LocalDateTime;

public interface Transaction {
    long getAmountSmallestUnit();

    String getCcy();

    LocalDateTime getBooked();

    void setBooked(LocalDateTime value);

    String getId();

    void setId(String value);

    Account getSenderAccount();

    void setSender(Account account);

    Wallet getSenderWallet();

    void setReceiver(Account account);

    Account getReceiverAccount();

    Wallet getReceiverWallet();

    void setReceiverWallet(Wallet wallet);

    void addStructuredReference(StructuredReference structuredReference);

    StructuredReference[] getStructuredReferences();

    void addMessage(String message);

    String[] getMessages();

    String getInvoiceId();

    void setInvoiceId(String s);

    Ledger getLedger();

    TransmissionState getTransmission();

    Address getSenderAddress();

    void setReceiverAddress(Address address);

    Address getReceiverAddress();

}
