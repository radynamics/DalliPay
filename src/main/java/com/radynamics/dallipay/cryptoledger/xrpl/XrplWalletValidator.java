package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.generic.GenericWalletValidator;
import com.radynamics.dallipay.cryptoledger.generic.WalletValidator;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationState;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class XrplWalletValidator implements WalletValidator {
    private final Ledger ledger;
    private final GenericWalletValidator genericValidator;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Validations");

    public XrplWalletValidator(Ledger ledger) {
        this.ledger = ledger;
        this.genericValidator = new GenericWalletValidator(ledger);
    }

    public ValidationResult[] validate(Wallet wallet, String senderOrReceiver) {
        var list = new ArrayList<>(List.of(genericValidator.validate(wallet, senderOrReceiver)));
        if (!list.isEmpty()) {
            return list.toArray(new ValidationResult[0]);
        }

        if (!ledger.exists(wallet)) {
            list.add(new ValidationResult(ValidationState.Error, String.format(res.getString("walletDoestExist"), senderOrReceiver)));
        }

        return list.toArray(new ValidationResult[0]);
    }

    @Override
    public boolean isValidFormat(Wallet wallet) {
        return genericValidator.isValidFormat(wallet);
    }

    @Override
    public ValidationResult validateFormat(Wallet wallet) {
        return genericValidator.validateFormat(wallet);
    }

    @Override
    public ValidationResult validateSecret(Wallet wallet) {
        return genericValidator.validateSecret(wallet);
    }
}
