package com.radynamics.CryptoIso20022Interop.cryptoledger;

import java.util.ArrayList;

public class TransactionResult {
    private ArrayList<Transaction> transactions = new ArrayList<>();
    private boolean hasMarker;
    private boolean hasMaxPageCounterReached;

    public void add(Transaction t) {
        transactions.add(t);
    }

    public Transaction[] transactions() {
        return transactions.toArray(new Transaction[0]);
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
}
