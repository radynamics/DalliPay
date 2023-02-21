package com.radynamics.dallipay.cryptoledger;

public interface TransactionLookupProvider {
    void open(String transactionId);
}
