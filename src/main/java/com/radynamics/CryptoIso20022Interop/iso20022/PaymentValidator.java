package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;

public interface PaymentValidator {
    ValidationResult[] validate(Payment t);
}
