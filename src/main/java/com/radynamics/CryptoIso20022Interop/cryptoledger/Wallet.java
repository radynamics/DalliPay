package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.google.common.primitives.UnsignedLong;

public interface Wallet {
    String getPublicKey();

    String getSecret();

    UnsignedLong getLedgerBalanceSmallestUnit();

    void setLedgerBalance(UnsignedLong amountSmallestUnit);
}
