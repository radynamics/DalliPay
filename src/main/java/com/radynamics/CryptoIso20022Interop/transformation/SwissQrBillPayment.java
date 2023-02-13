package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.cryptoledger.ExpectedCurrency;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.Origin;
import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.Address;
import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReferenceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SwissQrBillPayment {
    private final static Logger log = LogManager.getLogger(SwissQrBillPayment.class);
    private final Ledger ledger;

    public SwissQrBillPayment(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
    }

    public static boolean matches(String text) {
        if (text == null) {
            return false;
        }
        var lines = toLines(text);
        return lines.length >= 31 && lines[0].equals("SPC");
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
        payment.setReceiverAccount(new IbanAccount(lines[3]));
        payment.setOrigin(Origin.Manual);

        if (lines[5].length() > 0) {
            var a = new Address(lines[5]);
            a.setStreet(String.format("%s %s", lines[6], lines[7]));
            a.setZip(lines[8]);
            a.setCity(lines[9]);
            a.setCountryShort(lines[10]);
            payment.setReceiverAddress(a);
        }

        if (lines[21].length() > 0) {
            var a = new Address(lines[21]);
            a.setStreet(String.format("%s %s", lines[22], lines[23]));
            a.setZip(lines[24]);
            a.setCity(lines[25]);
            a.setCountryShort(lines[26]);
            payment.setSenderAddress(a);
        }

        if (lines[28].length() > 0) {
            var refType = StructuredReferenceFactory.detectType(lines[28]);
            payment.addStructuredReference(StructuredReferenceFactory.create(refType, lines[28]));
        }

        if (lines[29].length() > 0) {
            payment.addMessage(lines[29]);
        }

        var qrBillCc = new ArrayList<QrBillCc>();

        if (lines.length >= 33 && lines[32].length() != 0) {
            var o = toQrBillCc(lines[32]);
            if (o != null) {
                qrBillCc.add(o);
            }
        }
        if (lines.length >= 34 && lines[33].length() != 0) {
            var o = toQrBillCc(lines[33]);
            if (o != null) {
                qrBillCc.add(o);
            }
        }

        if (qrBillCc.size() == 0) {
            return payment;
        }

        var chosen = choose(qrBillCc);
        if (chosen == null) {
            return payment;
        }

        payment.setReceiverWallet(chosen.receiverWallet);
        if (chosen.expectedCcyIssuer != null) {
            payment.setExpectedCurrency(new ExpectedCurrency(chosen.expectedCcyIssuer));
        }

        return payment;
    }

    private QrBillCc choose(ArrayList<QrBillCc> candidates) {
        for (var c : candidates) {
            if (c.receiverWallet != null) {
                return c;
            }
        }
        return null;
    }

    private static Money parseAmount(String[] lines) {
        if (lines[18].length() == 0) {
            return null;
        }

        var dfs = DecimalFormatSymbols.getInstance();
        dfs.setDecimalSeparator('.');
        var formatString = "#########0.00";
        var df = new DecimalFormat(formatString, dfs);

        try {
            // Eg: "1000.50"
            return Money.of(df.parse(lines[18]).doubleValue(), new Currency(lines[19]));
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Parse AV according to https://www.radynamics.com/qr-bill-cc
     */
    private QrBillCc toQrBillCc(String av) {
        if (!av.startsWith("CC/XRPL")) {
            return null;
        }

        var elements = Arrays.asList(av.split("/"));

        var o = new QrBillCc();
        o.receiverWallet = toWalletOrNull(elements, "10");
        o.expectedCcyIssuer = toWalletOrNull(elements, "20");
        return o;
    }

    private Wallet toWalletOrNull(List<String> elements, String tag) {
        var index = elements.indexOf(tag);
        if (index == -1 || index + 1 >= elements.size()) {
            return null;
        }

        var walletText = elements.get(index + 1);
        return ledger.isValidPublicKey(walletText) ? ledger.createWallet(walletText, "") : null;
    }

    private static class QrBillCc {
        public Wallet receiverWallet;
        public Wallet expectedCcyIssuer;
    }
}
