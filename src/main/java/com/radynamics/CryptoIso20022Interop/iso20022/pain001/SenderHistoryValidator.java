package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.cryptoledger.PaymentHistoryProvider;
import com.radynamics.CryptoIso20022Interop.cryptoledger.PaymentUtils;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SenderHistoryValidator {
    private final Map<String, PaymentHistoryProvider> senderPaymentHistory = new HashMap<>();
    private final DateTimeFormatter df = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM);

    public ValidationResult[] validate(Payment[] payments) {
        var list = new ArrayList<ValidationResult>();

        for (var senderWallet : PaymentUtils.distinctSendingWallets(payments)) {
            for (var p : PaymentUtils.fromSender(senderWallet, payments)) {
                list.addAll(Arrays.asList(validate(p)));
            }
        }

        return list.toArray(new ValidationResult[0]);
    }

    public ValidationResult[] validate(Payment p) {
        if (p.getSenderWallet() == null) {
            return new ValidationResult[0];
        }

        var list = new ArrayList<ValidationResult>();
        var ledger = p.getLedger();
        if (!ledger.isValidPublicKey(p.getSenderWallet().getPublicKey())) {
            return new ValidationResult[0];
        }

        var key = p.getSenderWallet().getPublicKey();
        if (!senderPaymentHistory.containsKey(key)) {
            var paymentHistory = ledger.getPaymentHistoryProvider();
            paymentHistory.load(ledger, p.getSenderWallet(), LocalDateTime.now().minusDays(40));
            senderPaymentHistory.put(key, paymentHistory);
        }

        var paymentHistory = senderPaymentHistory.get(key);
        var similar = paymentHistory.oldestSimilarOrDefault(p);
        if (similar != null) {
            list.add(new ValidationResult(ValidationState.Warning, String.format("Similar payment sent to same receiver at %s.", df.format(similar.getBooked()))));
        }

        return list.toArray(new ValidationResult[0]);
    }

    public void clearCache() {
        senderPaymentHistory.clear();
    }
}