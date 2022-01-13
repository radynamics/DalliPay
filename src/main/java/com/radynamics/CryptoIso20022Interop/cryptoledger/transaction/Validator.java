package com.radynamics.CryptoIso20022Interop.cryptoledger.transaction;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;

import java.util.ArrayList;

public class Validator {
    public ValidationResult[] validate(Transaction t) {
        var list = new ArrayList<ValidationResult>();

        if (t.getSenderAccount() == null || t.getSenderAccount().getUnformatted().length() == 0) {
            list.add(new ValidationResult(ValidationState.Error, String.format("Sender account is missing")));
        }

        if (t.getReceiverWallet() == null) {
            list.add(new ValidationResult(ValidationState.Error, String.format("Receiver Cryptocurrency wallet is missing")));
        }

        return list.toArray(new ValidationResult[0]);
    }
}
