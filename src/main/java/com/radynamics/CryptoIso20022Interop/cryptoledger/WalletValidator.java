package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.Status;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;

import java.util.ArrayList;

public class WalletValidator {
    private Ledger ledger;

    public WalletValidator(Ledger ledger) {
        this.ledger = ledger;
    }

    public ValidationResult[] validate(Wallet wallet) {
        var list = new ArrayList<ValidationResult>();

        if (!ledger.exists(wallet)) {
            list.add(new ValidationResult(Status.Error, String.format("Receiver Cryptocurrency wallet doesn't exist.")));
        }

        return list.toArray(new ValidationResult[0]);
    }
}
