package com.radynamics.CryptoIso20022Interop.cryptoledger.transaction;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletValidator;

import java.util.ArrayList;
import java.util.Arrays;

public class Validator {
    public ValidationResult[] validate(Transaction t) {
        var list = new ArrayList<ValidationResult>();

        if (t.getReceiverWallet() == null) {
            list.add(new ValidationResult(ValidationState.Error, String.format("Receiver Cryptocurrency wallet is missing")));
        } else {
            var wv = new WalletValidator(t.getLedger());
            list.addAll(Arrays.asList(wv.validate(t.getReceiverWallet())));
        }

        if (t.getStructuredReferences().length == 0) {
            list.add(new ValidationResult(ValidationState.Info, String.format("Remittance info is missing. Receiver won't be able to match awaited payment exactly.")));
        }

        return list.toArray(new ValidationResult[0]);
    }
}
