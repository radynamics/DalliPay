package com.radynamics.dallipay.cryptoledger.ethereum;

import com.radynamics.dallipay.cryptoledger.transaction.ValidationResult;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.pain001.WalletHistoryValidator;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class PaymentValidator implements com.radynamics.dallipay.iso20022.PaymentValidator {
    private final Ledger ledger;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Validations");

    public PaymentValidator(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
    }

    @Override
    public ValidationResult[] validate(Payment t) {
        var list = new ArrayList<ValidationResult>();

        return list.toArray(new ValidationResult[0]);
    }

    @Override
    public WalletHistoryValidator getHistoryValidator() {
        return null;
    }

    @Override
    public void clearCache() {
        // do nothing
    }
}
