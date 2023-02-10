package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.Origin;
import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.Address;
import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.ReferenceType;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReferenceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

public class EpcPayment {
    private final static Logger log = LogManager.getLogger(SwissQrBillPayment.class);
    private final Ledger ledger;

    public EpcPayment(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
    }

    public static boolean matches(String text) {
        if (text == null) {
            return false;
        }
        var lines = toLines(text);
        return lines.length >= 11 && lines[0].equals("BCD");
    }

    private static String[] toLines(String text) {
        return text.split("\\r?\\n");
    }

    public Payment createOrNull(String text) {
        if (!matches(text)) {
            return null;
        }

        var lines = toLines(text);

        var payment = new Payment(ledger.createTransaction());
        var amount = parseAmount(lines);
        payment.setAmount(amount != null ? amount : Money.zero(new Currency(ledger.getNativeCcySymbol())));
        payment.setReceiverAccount(new IbanAccount(lines[6]));
        payment.setOrigin(Origin.Manual);

        if (lines[5].length() > 0) {
            payment.setReceiverAddress(new Address(lines[5]));
        }

        if (lines[9].length() > 0) {
            payment.addStructuredReference(StructuredReferenceFactory.create(ReferenceType.Scor, lines[9]));
        }

        if (lines[10].length() > 0) {
            payment.addMessage(lines[10]);
        }

        return payment;
    }

    private static Money parseAmount(String[] lines) {
        if (lines[7].length() == 0) {
            return null;
        }

        var dfs = DecimalFormatSymbols.getInstance();
        dfs.setDecimalSeparator('.');
        var formatString = "#########0.00";
        var df = new DecimalFormat(formatString, dfs);

        try {
            // Eg: "EUR123.45"
            var ccy = lines[7].substring(0, 3);
            var amt = lines[7].substring(3);
            return Money.of(df.parse(amt).doubleValue(), new Currency(ccy));
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }
}
