package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.google.common.primitives.UnsignedLong;

public interface Wallet {
    String getPublicKey();

    String getSecret();

    void setSecret(String secret);

    UnsignedLong getLedgerBalanceSmallestUnit();

    void setLedgerBalance(UnsignedLong amountSmallestUnit);

    LedgerId getLedgerId();
}
