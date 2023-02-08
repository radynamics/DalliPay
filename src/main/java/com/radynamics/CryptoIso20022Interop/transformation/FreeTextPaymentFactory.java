package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

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
