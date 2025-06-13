package com.radynamics.dallipay.cryptoledger.generic;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationState;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class GenericWalletValidator implements WalletValidator {
    private Ledger ledger;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Validations");

    public GenericWalletValidator(Ledger ledger) {
        this.ledger = ledger;
    }

    public ValidationResult[] validate(com.radynamics.dallipay.cryptoledger.Wallet wallet, String senderOrReceiver) {
        var list = new ArrayList<ValidationResult>();

        var formatResult = validateFormat(wallet, senderOrReceiver);
        if (formatResult != null) {
            list.add(formatResult);
            return list.toArray(new ValidationResult[0]);
        }

        return list.toArray(new ValidationResult[0]);
    }

    public boolean isValidFormat(com.radynamics.dallipay.cryptoledger.Wallet wallet) {
        return validateFormat(wallet) == null;
    }

    public ValidationResult validateFormat(com.radynamics.dallipay.cryptoledger.Wallet wallet) {
        return validateFormat(wallet, null);
    }

    private ValidationResult validateFormat(com.radynamics.dallipay.cryptoledger.Wallet wallet, String senderOrReceiver) {
        var prefix = senderOrReceiver == null ? "" : String.format("%s ", senderOrReceiver);
        return wallet != null && ledger.isValidWallet(wallet.getPublicKey())
                ? null
                : new ValidationResult(ValidationState.Error, String.format(res.getString("walletInvalid"), prefix));
    }

    public ValidationResult validateSecret(Wallet wallet) {
        return ledger.isSecretValid(wallet)
                ? null
                : new ValidationResult(ValidationState.Error, String.format(res.getString("walletSecretInvalid"), wallet.getPublicKey()));
    }
}
