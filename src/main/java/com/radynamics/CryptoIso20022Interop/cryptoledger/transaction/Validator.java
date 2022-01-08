package com.radynamics.CryptoIso20022Interop.cryptoledger.transaction;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;

import java.util.ArrayList;

public class Validator {
    public ValidationResult[] validate(Transaction t) {
        var list = new ArrayList<ValidationResult>();

        if (t.getReceiverWallet() == null) {
            list.add(new ValidationResult(Status.Error, String.format("Receiver is missing")));
        }

        return list.toArray(new ValidationResult[0]);
    }
}
