package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

public interface WalletHistoryValidator {
    ValidationResult[] validate(Payment p);

    void loadHistory(Ledger ledger, Wallet wallet);

    void clearCache();
}
