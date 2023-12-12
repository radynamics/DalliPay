package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.PaymentConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Comparator;

public class LedgerPaymentHistoryProvider implements PaymentHistoryProvider {
    final static Logger log = LogManager.getLogger(LedgerPaymentHistoryProvider.class);
    private Transaction[] transactions = new Transaction[0];

    @Override
    public void load(Ledger ledger, Wallet wallet, long sinceDaysAgo) {
        try {
            var transactionResult = ledger.listPaymentsSent(wallet, sinceDaysAgo, 1000);
            transactions = Arrays.stream(transactionResult.transactions()).sorted(Comparator.comparing(Transaction::getBooked)).toArray(Transaction[]::new);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public Transaction oldestSimilarOrDefault(Payment p) {
        var c = new PaymentComparer();
        for (var t : transactions) {
            if (c.similar(PaymentConverter.toPayment(t, p.getUserCcy()), p)) {
                return t;
            }
        }
        return null;
    }
}
