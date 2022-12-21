package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.xumm;

public interface OAuth2PkceListener {
    void onAuthorizationCodeReceived(String code);
}
