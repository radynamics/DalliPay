package com.radynamics.dallipay.cryptoledger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class TransactionResult {
    private ArrayList<Transaction> transactions = new ArrayList<>();
    private boolean hasMarker;
    private boolean hasMaxPageCounterReached;
    private boolean hasNoTransactions;
    private boolean existsWallet;

    public void add(Transaction t) {
        transactions.add(t);
    }

    public Transaction[] transactions() {
        var array = transactions.toArray(new Transaction[0]);
        Arrays.sort(array, Comparator.comparing(Transaction::getBooked).reversed());
        return array;
    }

    public boolean hasMarker() {
        return hasMarker;
    }

    public void setHasMarker(boolean hasMarker) {
        this.hasMarker = hasMarker;
    }

    public void setHasMaxPageCounterReached(boolean hasMaxPageCounterReached) {
        this.hasMaxPageCounterReached = hasMaxPageCounterReached;
    }

    public boolean hasMaxPageCounterReached() {
        return hasMaxPageCounterReached;
    }

    public void setHasNoTransactions(boolean value) {
        hasNoTransactions = value;
    }

    public boolean hasNoTransactions() {
        return hasNoTransactions;
    }

    public boolean existsWallet() {
        return existsWallet;
    }

    public void setExistsWallet(boolean existsWallet) {
        this.existsWallet = existsWallet;
    }
}
