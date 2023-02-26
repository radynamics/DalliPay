package com.radynamics.dallipay.cryptoledger.transaction;

import com.radynamics.dallipay.iso20022.Payment;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class Validator {
    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Validations");

    public ValidationResult[] validate(Payment t) {
        var list = new ArrayList<ValidationResult>();

        if (t.getReceiverWallet() == null) {
            list.add(new ValidationResult(ValidationState.Error, res.getString("receiverWalletMissing")));
        }

        return list.toArray(new ValidationResult[0]);
    }
}
