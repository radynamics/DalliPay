package com.radynamics.dallipay.cryptoledger.generic;

import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;

public interface WalletValidator {
    ValidationResult[] validate(com.radynamics.dallipay.cryptoledger.Wallet wallet, String senderOrReceiverLabel);

    boolean isValidFormat(com.radynamics.dallipay.cryptoledger.Wallet wallet);

    ValidationResult validateFormat(com.radynamics.dallipay.cryptoledger.Wallet wallet);

    ValidationResult validateSecret(Wallet wallet);
}
