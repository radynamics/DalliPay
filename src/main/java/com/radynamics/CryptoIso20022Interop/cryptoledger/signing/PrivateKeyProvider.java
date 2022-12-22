package com.radynamics.CryptoIso20022Interop.cryptoledger.signing;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

public interface PrivateKeyProvider {
    /**
     * Returns a privateKey for a given publicKey.
     *
     * @param publicKey
     * @return Null, if no value is available
     */
    String get(String publicKey);

    /**
     * Collects all privateKeys for all sender addresses of given payments
     *
     * @param payments
     * @return
     */
    boolean collect(Payment[] payments);
}
