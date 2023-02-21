package com.radynamics.dallipay.iso20022;

import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;
import com.radynamics.dallipay.iso20022.pain001.WalletHistoryValidator;

public interface PaymentValidator {
    ValidationResult[] validate(Payment t);

    WalletHistoryValidator getHistoryValidator();

    void clearCache();
}
