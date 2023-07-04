package com.radynamics.dallipay.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.http.HttpHeaders;

public class ApiRateLimitLogger {
    private final static Logger log = LogManager.getLogger(ApiRateLimitLogger.class);
    private final String apiName;

    public ApiRateLimitLogger(String apiName) {
        if (apiName == null) throw new IllegalArgumentException("Parameter 'apiName' cannot be null");
        this.apiName = apiName;
    }

    /**
     * Returns false if limit is reached.
     */
    public boolean log(HttpHeaders headers) {
        final String nameRemaining = "X-RateLimit-Remaining";
        var remainingText = headers.firstValue(nameRemaining).orElse(null);
        if (remainingText == null) {
            log.warn(String.format("%s: Header %s was not present in response.", apiName, nameRemaining));
            return true;
        }

        var limitText = headers.firstValue("X-RateLimit-Limit").orElse("0");
        var msg = String.format("%s: emaining %s/%s calls within rateLimit.", apiName, remainingText, limitText);
        if (Integer.parseInt(remainingText) >= 10) {
            log.info(msg);
            return true;
        }
        if (Integer.parseInt(remainingText) > 0) {
            log.warn(msg);
            return true;
        }

        log.error(String.format("%s: call limit reached. %s", apiName, msg));
        return false;
    }
}
