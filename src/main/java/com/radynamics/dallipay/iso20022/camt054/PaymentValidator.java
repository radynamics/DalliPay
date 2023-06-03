package com.radynamics.dallipay.iso20022.camt054;

import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;
import com.radynamics.dallipay.cryptoledger.transaction.ValidationState;
import com.radynamics.dallipay.cryptoledger.transaction.Validator;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.pain001.NullHistoryValidator;
import com.radynamics.dallipay.iso20022.pain001.WalletHistoryValidator;
import com.radynamics.dallipay.ui.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class PaymentValidator implements com.radynamics.dallipay.iso20022.PaymentValidator {
    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Validations");

    public ValidationResult[] validate(Payment t) {
        var list = new ArrayList<ValidationResult>();
        list.addAll(Arrays.asList(new Validator().validate(t)));

        if (t.isAmountUnknown()) {
            list.add(new ValidationResult(ValidationState.Error, String.format(res.getString("amountUnknown"), Utils.createFormatDate().format(t.getBooked()))));
        }

        return list.toArray(new ValidationResult[0]);
    }

    @Override
    public WalletHistoryValidator getHistoryValidator() {
        return new NullHistoryValidator();
    }

    @Override
    public void clearCache() {
        // do nothing
    }
}
