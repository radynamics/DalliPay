package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.WalletHistoryValidator;

public interface PaymentValidator {
    ValidationResult[] validate(Payment t);

    WalletHistoryValidator getHistoryValidator();
}
