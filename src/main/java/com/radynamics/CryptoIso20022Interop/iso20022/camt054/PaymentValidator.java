package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.Validator;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;

public class PaymentValidator implements com.radynamics.CryptoIso20022Interop.iso20022.PaymentValidator {
    private final DateTimeFormatter df = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    public ValidationResult[] validate(Payment t) {
        var list = new ArrayList<ValidationResult>();
        list.addAll(Arrays.asList(new Validator().validate(t)));

        if (t.getSenderAccount().getUnformatted().equalsIgnoreCase(t.getSenderWallet().getPublicKey())) {
            list.add(new ValidationResult(ValidationState.Info, String.format("No Account/IBAN defined for this CryptoCurrency Wallet and therefore Wallet address is exported.")));
        }

        if (t.isAmountUnknown()) {
            list.add(new ValidationResult(ValidationState.Error, String.format("Amount is unknown. Mostly because no FX rate was found at %s.", df.format(t.getBooked()))));
        }

        return list.toArray(new ValidationResult[0]);
    }
}
