package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.Validator;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.NullHistoryValidator;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.WalletHistoryValidator;
import com.radynamics.CryptoIso20022Interop.ui.Utils;

import java.util.ArrayList;
import java.util.Arrays;

public class PaymentValidator implements com.radynamics.CryptoIso20022Interop.iso20022.PaymentValidator {
    public ValidationResult[] validate(Payment t) {
        var list = new ArrayList<ValidationResult>();
        list.addAll(Arrays.asList(new Validator().validate(t)));

        if (t.getSenderAccount() != null && t.getSenderAccount().getUnformatted().equalsIgnoreCase(t.getSenderWallet().getPublicKey())) {
            list.add(new ValidationResult(ValidationState.Info, String.format("No Account/IBAN defined for this CryptoCurrency Wallet and therefore Wallet address is exported.")));
        }

        if (t.isAmountUnknown()) {
            list.add(new ValidationResult(ValidationState.Error, String.format("Amount is unknown due no exchange rate was found at %s.", Utils.createFormatDate().format(t.getBooked()))));
        }

        return list.toArray(new ValidationResult[0]);
    }

    @Override
    public WalletHistoryValidator getHistoryValidator() {
        return new NullHistoryValidator();
    }
}
