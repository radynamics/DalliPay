package com.radynamics.dallipay.browserwalletbridge.metamask;

import com.radynamics.dallipay.browserwalletbridge.BrowserApi;

public class MetaMask implements BrowserApi {
    @Override
    public String getContentRoot() {
        return "metamask";
    }

    @Override
    public PayloadConverter createPayloadConverter() {
        return new PayloadConverter();
    }
}
