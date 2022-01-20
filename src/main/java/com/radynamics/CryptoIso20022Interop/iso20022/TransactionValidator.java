package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;

// TODO: 2022-01-20 rename into PaymentValidator
public interface TransactionValidator {
    ValidationResult[] validate(Payment t);
}
