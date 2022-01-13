package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;

public interface TransactionValidator {
    ValidationResult[] validate(Transaction t);
}
