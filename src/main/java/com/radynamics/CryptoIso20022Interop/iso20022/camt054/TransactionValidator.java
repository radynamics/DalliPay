package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.Validator;

import java.util.ArrayList;
import java.util.Arrays;

public class TransactionValidator implements com.radynamics.CryptoIso20022Interop.iso20022.TransactionValidator {
    public ValidationResult[] validate(Transaction t) {
        var list = new ArrayList<ValidationResult>();
        list.addAll(Arrays.asList(new Validator().validate(t)));

        if (t.getSenderAccount().getUnformatted().equalsIgnoreCase(t.getSenderWallet().getPublicKey())) {
            list.add(new ValidationResult(ValidationState.Info, String.format("No Account/IBAN defined for this CryptoCurrency Wallet and therefore Wallet address is exported.")));
        }

        return list.toArray(new ValidationResult[0]);
    }
}
