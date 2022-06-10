package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.DateTimeConvert;
import com.radynamics.CryptoIso20022Interop.cryptoledger.PaymentHistoryProvider;
import com.radynamics.CryptoIso20022Interop.cryptoledger.PaymentUtils;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationState;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.Utils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SenderHistoryValidator implements WalletHistoryValidator {
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

        var key = createKey(p.getSenderWallet());
        synchronized (this) {
            if (!senderPaymentHistory.containsKey(key)) {
                loadHistory(p);
            }
        }

        var paymentHistory = senderPaymentHistory.get(key);
        var similar = paymentHistory.oldestSimilarOrDefault(p);
        if (similar != null) {
            list.add(new ValidationResult(ValidationState.Warning, String.format("Similar payment sent to same receiver at %s.", df.format(DateTimeConvert.toUserTimeZone(similar.getBooked())))));
        }

        return list.toArray(new ValidationResult[0]);
    }

    public void clearCache() {
        senderPaymentHistory.clear();
    }

    private String createKey(Wallet wallet) {
        return wallet.getPublicKey();
    }

    private void loadHistory(Payment p) {
        loadHistory(new Payment[]{p});
    }

    public void loadHistory(Payment[] payments) {
        for (var p : payments) {
            if (p.getSenderWallet() == null) {
                continue;
            }

            var key = createKey(p.getSenderWallet());
            if (!senderPaymentHistory.containsKey(key)) {
                var ledger = p.getLedger();
                var paymentHistory = ledger.getPaymentHistoryProvider();

                var desired = ZonedDateTime.now().minusDays(40);
                var availableSince = ledger.getNetwork().historyAvailableSince();
                var since = desired.isBefore(availableSince) ? availableSince : desired;

                // Use endOfDay to ensure data until latest ledger is loaded. Ignoring time improves cache hits.
                paymentHistory.load(ledger, p.getSenderWallet(), Utils.endOfDay(since));
                senderPaymentHistory.put(key, paymentHistory);
            }
        }
    }
}