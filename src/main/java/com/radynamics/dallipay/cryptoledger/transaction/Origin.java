package com.radynamics.dallipay.cryptoledger.transaction;

public enum Origin {
    Manual,
    Imported,
    Ledger,
    ;

    public boolean isDeletable() {
        return this == Manual;
    }
}
