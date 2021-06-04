package com.radynamics.CryptoIso20022Interop.transformation;

public class AccountMapping {
    public String iban;
    public String walletPublicKey;

    public AccountMapping(String iban, String walletPublicKey) {
        this.iban = iban;
        this.walletPublicKey = walletPublicKey;
    }
}
