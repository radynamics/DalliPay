package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;

public final class PaymentConverter {
    public static Payment[] toPayment(Transaction[] transactions) {
        var items = new Payment[transactions.length];
        for (var i = 0; i < transactions.length; i++) {
            items[i] = toPayment(transactions[i]);
        }
        return items;
    }

    public static Payment toPayment(Transaction t) {
        return new Payment(t);
    }

    public static Transaction[] toTransactions(Payment[] payments) {
        var items = new Transaction[payments.length];
        for (var i = 0; i < payments.length; i++) {
            items[i] = payments[i].getTransaction();
        }
        return items;
    }
}
