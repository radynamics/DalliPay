package com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm;

import java.net.URI;

public interface OAuth2PkceAuthenticationListener {
    void onAuthorized(String accessToken);

    void onOpenInBrowser(URI uri);
}
