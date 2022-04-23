package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.time.LocalDateTime;

public interface PaymentHistoryProvider {
    void load(Ledger ledger, Wallet wallet, LocalDateTime since);

    Transaction oldestSimilarOrDefault(Payment p);
}
