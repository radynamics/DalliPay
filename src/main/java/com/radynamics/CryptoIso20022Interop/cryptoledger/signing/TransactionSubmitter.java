package com.radynamics.CryptoIso20022Interop.cryptoledger.signing;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;

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
}
