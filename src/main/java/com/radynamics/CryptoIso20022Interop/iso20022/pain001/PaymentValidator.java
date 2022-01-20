package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletValidator;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.Validator;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.util.ArrayList;
import java.util.Arrays;

public class PaymentValidator implements com.radynamics.CryptoIso20022Interop.iso20022.PaymentValidator {
    public ValidationResult[] validate(Payment t) {
        var list = new ArrayList<ValidationResult>();
        list.addAll(Arrays.asList(new Validator().validate(t)));

        if (t.getReceiverWallet() != null) {
            var wv = new WalletValidator(t.getLedger());
            list.addAll(Arrays.asList(wv.validate(t.getReceiverWallet())));
        }

        if (t.getStructuredReferences().length == 0) {
            list.add(new ValidationResult(ValidationState.Info, String.format("Remittance info is missing. Receiver won't be able to match awaited payment exactly.")));
        }

        return list.toArray(new ValidationResult[0]);
    }
}
