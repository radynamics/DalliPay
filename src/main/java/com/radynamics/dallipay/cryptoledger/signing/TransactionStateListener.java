package com.radynamics.dallipay.cryptoledger.signing;

import com.radynamics.dallipay.cryptoledger.Transaction;

public interface TransactionStateListener {
    void onProgressChanged(Transaction t);

    void onSuccess(Transaction t);

    void onFailure(Transaction t);
}
