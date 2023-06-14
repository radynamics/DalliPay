package com.radynamics.dallipay.browserwalletbridge;

public interface BrowserApi {
    String getContentRoot();

    PayloadConverter createPayloadConverter();
}
