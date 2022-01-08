package com.radynamics.CryptoIso20022Interop.cryptoledger.transaction;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;

import java.util.ArrayList;

public class Validator {
    public ValidationResult[] validate(Transaction t) {
        var list = new ArrayList<ValidationResult>();

        if (t.getReceiverWallet() == null) {
            list.add(new ValidationResult(Status.Error, String.format("Receiver is missing")));
        }

        if (t.getStructuredReferences().length == 0) {
            list.add(new ValidationResult(Status.Info, String.format("Remittance info is missing. Receiver won't be able to match to awaited payment exactly.")));
        }
        
        return list.toArray(new ValidationResult[0]);
    }
}
