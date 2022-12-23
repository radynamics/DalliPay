package com.radynamics.CryptoIso20022Interop.cryptoledger.signing;

import com.radynamics.CryptoIso20022Interop.cryptoledger.LedgerException;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;

import java.util.function.Function;

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
    void submit(Transaction t, T builder, Function<String, Void> onSuccess) throws LedgerException;

    PrivateKeyProvider getPrivateKeyProvider();

    void addStateListener(TransactionStateListener l);
}
