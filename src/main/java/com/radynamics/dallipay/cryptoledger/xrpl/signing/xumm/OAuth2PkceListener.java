package com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm;

public interface OAuth2PkceListener {
    void onAuthorizationCodeReceived(String code);
}
