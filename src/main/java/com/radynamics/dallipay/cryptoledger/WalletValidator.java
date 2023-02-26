package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationState;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class WalletValidator {
    private Ledger ledger;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Validations");

    public WalletValidator(Ledger ledger) {
        this.ledger = ledger;
    }

    public ValidationResult[] validate(Wallet wallet, String senderOrReceiver) {
        var list = new ArrayList<ValidationResult>();

        var formatResult = validateFormat(wallet, senderOrReceiver);
        if (formatResult != null) {
            list.add(formatResult);
            return list.toArray(new ValidationResult[0]);
        }

        if (!ledger.exists(wallet)) {
            list.add(new ValidationResult(ValidationState.Error, String.format(res.getString("walletDoestExist"), senderOrReceiver)));
        }

        return list.toArray(new ValidationResult[0]);
    }

    public static boolean isValidFormat(Ledger ledger, Wallet wallet) {
        var v = new WalletValidator(ledger);
        return v.validateFormat(wallet) == null;
    }

    public ValidationResult validateFormat(Wallet wallet) {
        return validateFormat(wallet, null);
    }

    private ValidationResult validateFormat(Wallet wallet, String senderOrReceiver) {
        var prefix = senderOrReceiver == null ? "" : String.format("%s ", senderOrReceiver);
        return wallet != null && ledger.isValidPublicKey(wallet.getPublicKey())
                ? null
                : new ValidationResult(ValidationState.Error, String.format(res.getString("walletInvalid"), prefix));
    }

    public ValidationResult validateSecret(Wallet wallet) {
        return ledger.isSecretValid(wallet)
                ? null
                : new ValidationResult(ValidationState.Error, String.format(res.getString("walletSecretInvalid"), wallet.getPublicKey()));
    }
}
