package com.radynamics.CryptoIso20022Interop.cryptoledger.signing;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;

public interface TransactionStateListener {
    void onSuccess(Transaction t);

    void onFailure(Transaction t);
}
