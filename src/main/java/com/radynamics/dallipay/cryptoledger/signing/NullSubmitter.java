package com.radynamics.dallipay.cryptoledger.signing;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Transaction;
import jakarta.ws.rs.NotSupportedException;

public class NullSubmitter implements TransactionSubmitter {
    private final Ledger ledger;

    public NullSubmitter(Ledger ledger) {
        this.ledger = ledger;
    }

    @Override
    public String getId() {
        return "nullSubmitter";
    }

    @Override
    public Ledger getLedger() {
        return ledger;
    }

    @Override
    public void submit(Transaction[] transactions) {
        // do nothing
    }

    @Override
    public PrivateKeyProvider getPrivateKeyProvider() {
        return null;
    }

    @Override
    public TransactionSubmitterInfo getInfo() {
        return null;
    }

    @Override
    public void addStateListener(TransactionStateListener l) {
        throw new NotSupportedException();
    }

    @Override
    public boolean supportsPathFinding() {
        return false;
    }
}
