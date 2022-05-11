package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

public class NullHistoryValidator implements WalletHistoryValidator {
    @Override
    public ValidationResult[] validate(Payment p) {
        return new ValidationResult[0];
    }

    @Override
    public void loadHistory(Payment[] payments) {
        // do nothing
    }

    @Override
    public void clearCache() {
        // do nothing
    }
}
