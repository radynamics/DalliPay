package com.radynamics.dallipay.transformation;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.iso20022.Payment;

public class FreeTextPaymentFactory {
    private final Ledger ledger;

    public FreeTextPaymentFactory(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
    }

    public Payment createOrNull(String text) {
        if (text == null || text.length() == 0) {
            return null;
        }

        if (LedgerWalletAddressPayment.matches(ledger, text)) {
            var o = new LedgerWalletAddressPayment(ledger);
            return o.createOrNull(text);
        }

        if (SwissQrBillPayment.matches(text)) {
            var o = new SwissQrBillPayment(ledger);
            return o.createOrNull(text);
        }

        if (EpcPayment.matches(text)) {
            var o = new EpcPayment(ledger);
            return o.createOrNull(text);
        }

        return null;
    }
}
