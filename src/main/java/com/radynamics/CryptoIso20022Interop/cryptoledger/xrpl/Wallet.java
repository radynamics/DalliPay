package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

public class Wallet implements com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet {

    private String publicKey;
    private String secret;

    public Wallet(String publicKey) {
        this(publicKey, null);
    }

    public Wallet(String publicKey, String secret) {
        this.publicKey = publicKey;
        this.secret = secret;
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public String getSecret() {
        return secret;
    }
}
