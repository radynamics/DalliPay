package com.radynamics.dallipay.cryptoledger.signing;

import com.radynamics.dallipay.iso20022.Payment;

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
