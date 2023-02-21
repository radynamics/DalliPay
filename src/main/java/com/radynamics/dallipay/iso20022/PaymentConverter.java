package com.radynamics.dallipay.iso20022;

import com.radynamics.dallipay.cryptoledger.Transaction;
import com.radynamics.dallipay.cryptoledger.transaction.Origin;
import com.radynamics.dallipay.exchange.Currency;

public final class PaymentConverter {
    public static Payment[] toPayment(Transaction[] transactions, Currency targetCcy) {
        var items = new Payment[transactions.length];
        for (var i = 0; i < transactions.length; i++) {
            items[i] = toPayment(transactions[i], getTargetCurrency(transactions[i], targetCcy));
        }
        return items;
    }

    public static Payment toPayment(Transaction t, Currency targetCcy) {
        var p = new Payment(t);
        p.setUserCcy(targetCcy);
        p.setOrigin(Origin.Ledger);
        return p;
    }

    public static Transaction[] toTransactions(Payment[] payments) {
        var items = new Transaction[payments.length];
        for (var i = 0; i < payments.length; i++) {
            items[i] = payments[i].getTransaction();
        }
        return items;
    }

    private static Currency getTargetCurrency(Transaction t, Currency targetCcySuggested) {
        var transactionCcy = t.getAmount().getCcy();
        if (targetCcySuggested == null || targetCcySuggested.getCode().equals(transactionCcy.getCode())) {
            return transactionCcy;
        }

        return targetCcySuggested;
    }
}
