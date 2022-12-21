package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.xumm;

import java.net.URI;

public interface OAuth2PkceAuthenticationListener {
    void onAuthorized(String accessToken);

    void onOpenInBrowser(URI uri);
}
