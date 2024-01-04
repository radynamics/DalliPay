package com.radynamics.dallipay.cryptoledger.signing;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Transaction;

/**
 * A type able to submit transactions to a ledger.
 */
public interface TransactionSubmitter {
    String getId();

    Ledger getLedger();

    void submit(Transaction[] transactions);

    PrivateKeyProvider getPrivateKeyProvider();

    TransactionSubmitterInfo getInfo();

    void addStateListener(TransactionStateListener l);

    boolean supportIssuedTokens();

    boolean supportsPathFinding();

    boolean supportsPayload();

    void deleteSettings();
}
