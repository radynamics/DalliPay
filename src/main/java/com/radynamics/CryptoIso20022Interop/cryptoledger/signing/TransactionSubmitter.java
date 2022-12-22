package com.radynamics.CryptoIso20022Interop.cryptoledger.signing;

import com.radynamics.CryptoIso20022Interop.cryptoledger.LedgerException;

/**
 * A typeo able to submit transactions to a ledger.
 *
 * @param <T> Type of the object (transaction) to submit.
 */
public interface TransactionSubmitter<T> {
    /**
     * Submit payment
     *
     * @param builder
     * @return TransactionId or null, if the payment submit failed.
     * @throws LedgerException
     */
    String submit(T builder) throws LedgerException;

    PrivateKeyProvider getPrivateKeyProvider();
}
