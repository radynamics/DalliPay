package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.iso20022.Payment;

public interface PaymentHistoryProvider {
    void load(Ledger ledger, Wallet wallet, long sinceDaysAgo);

    Transaction oldestSimilarOrDefault(Payment p);
}
