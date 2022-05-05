package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.PaymentConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;

public class LedgerPaymentHistoryProvider implements PaymentHistoryProvider {
    final static Logger log = LogManager.getLogger(LedgerPaymentHistoryProvider.class);
    private Transaction[] transactions = new Transaction[0];

    @Override
    public void load(Ledger ledger, Wallet wallet, ZonedDateTime since) {
        try {
            var transactionResult = ledger.listPaymentsSent(wallet, since, 1000);
            transactions = Arrays.stream(transactionResult.transactions()).sorted(Comparator.comparing(Transaction::getBooked)).toArray(Transaction[]::new);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public Transaction oldestSimilarOrDefault(Payment p) {
        for (var t : transactions) {
            if (PaymentCompare.isSimilar(PaymentConverter.toPayment(t, p.getFiatCcy()), p)) {
                return t;
            }
        }
        return null;
    }
}
