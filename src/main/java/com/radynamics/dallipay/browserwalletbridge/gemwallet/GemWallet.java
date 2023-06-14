package com.radynamics.dallipay.browserwalletbridge.gemwallet;

import com.radynamics.dallipay.browserwalletbridge.BrowserApi;

public class GemWallet implements BrowserApi {
    @Override
    public String getContentRoot() {
        return "gemwallet";
    }

    @Override
    public PayloadConverter createPayloadConverter() {
        return new PayloadConverter();
    }
}
