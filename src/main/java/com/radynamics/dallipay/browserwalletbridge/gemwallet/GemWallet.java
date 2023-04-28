package com.radynamics.dallipay.browserwalletbridge.gemwallet;

import com.radynamics.dallipay.browserwalletbridge.BrowserApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GemWallet implements BrowserApi {
    private final static Logger log = LogManager.getLogger(GemWallet.class);

    @Override
    public String createSendRequestResponse() {
        var is = getClass().getClassLoader().getResourceAsStream("browserwalletbridge/gemwallet/index.html");
        try {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
