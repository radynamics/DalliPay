package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.WalletHistoryValidator;

public class EmptyPaymentValidator implements PaymentValidator {
    @Override
    public ValidationResult[] validate(Payment t) {
        return new ValidationResult[0];
    }

    @Override
    public WalletHistoryValidator getHistoryValidator() {
        return null;
    }

    @Override
    public void clearCache() {
        // do nothing
    }
}
