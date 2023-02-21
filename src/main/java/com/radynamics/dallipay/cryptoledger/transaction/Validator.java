package com.radynamics.dallipay.cryptoledger.transaction;

import com.radynamics.dallipay.iso20022.Payment;

import java.util.ArrayList;

public class Validator {
    public ValidationResult[] validate(Payment t) {
        var list = new ArrayList<ValidationResult>();

        if (t.getReceiverWallet() == null) {
            list.add(new ValidationResult(ValidationState.Error, String.format("Receiver wallet is missing")));
        }

        return list.toArray(new ValidationResult[0]);
    }
}
