package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.time.ZonedDateTime;

public interface PaymentHistoryProvider {
    void load(Ledger ledger, Wallet wallet, ZonedDateTime since);

    Transaction oldestSimilarOrDefault(Payment p);
}
