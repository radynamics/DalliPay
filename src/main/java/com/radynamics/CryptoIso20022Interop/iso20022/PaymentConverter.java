package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;

public final class PaymentConverter {
    public static Payment[] toPayment(Transaction[] transactions, String targetCcy) {
        var items = new Payment[transactions.length];
        for (var i = 0; i < transactions.length; i++) {
            items[i] = toPayment(transactions[i], targetCcy);
        }
        return items;
    }

    public static Payment toPayment(Transaction t, String targetCcy) {
        var p = new Payment(t);
        p.setFiatCcy(targetCcy);
        return p;
    }

    public static Transaction[] toTransactions(Payment[] payments) {
        var items = new Transaction[payments.length];
        for (var i = 0; i < payments.length; i++) {
            items[i] = payments[i].getTransaction();
        }
        return items;
    }
}
