package com.radynamics.CryptoIso20022Interop.cryptoledger.signing;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;

/**
 * A type able to submit transactions to a ledger.
 *
 * @param <T> Type of the object (transaction) to submit.
 */
public interface TransactionSubmitter<T> {
    void submit(Transaction[] transactions);

    PrivateKeyProvider getPrivateKeyProvider();

    void addStateListener(TransactionStateListener l);
}
