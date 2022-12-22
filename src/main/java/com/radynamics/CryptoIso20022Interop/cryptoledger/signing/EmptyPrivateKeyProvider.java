package com.radynamics.CryptoIso20022Interop.cryptoledger.signing;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

public class EmptyPrivateKeyProvider implements PrivateKeyProvider {
    @Override
    public String get(String publicKey) {
        return null;
    }

    @Override
    public boolean collect(Payment[] payments) {
        return true;
    }
}
