package com.radynamics.dallipay.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.http.HttpHeaders;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ApiRateLimitLogger {
    private final static Logger log = LogManager.getLogger(ApiRateLimitLogger.class);
    private final String apiName;
    private ZonedDateTime retryAfter = ZonedDateTime.now();

    public ApiRateLimitLogger(String apiName) {
        if (apiName == null) throw new IllegalArgumentException("Parameter 'apiName' cannot be null");
        this.apiName = apiName;
    }

    /**
     * Returns false if limit is reached.
     */
    public boolean log(HttpHeaders headers) {
        return log(headers.map());
    }

    /**
     * Returns false if limit is reached.
     */
    public boolean log(Map<String, List<String>> headers) {
        final String nameRemaining = "X-RateLimit-Remaining";
        var remainingText = first(headers, nameRemaining).orElse(null);
        if (remainingText == null) {
            log.warn(String.format("%s: Header %s was not present in response.", apiName, nameRemaining));
            return true;
        }

        var limitText = first(headers, "X-RateLimit-Limit").orElse("0");
        var msg = String.format("%s: remaining %s/%s calls within rateLimit.", apiName, remainingText, limitText);
        if (Integer.parseInt(remainingText) >= 10) {
            log.info(msg);
            return true;
        }
        if (Integer.parseInt(remainingText) > 0) {
            log.warn(msg);
            return true;
        }

        var retryAfterText = first(headers, "Retry-After").orElse(null);
        if (retryAfterText != null) {
            retryAfter = ZonedDateTime.parse(retryAfterText).withZoneSameInstant(ZoneId.of("UTC"));
        }

        log.error(String.format("%s: call limit reached. %s", apiName, msg));
        return false;
    }

    private Optional<String> first(Map<String, List<String>> map, String key) {
        List<String> elements = new ArrayList<>();
        for (var k : map.entrySet()) {
            if (key.equalsIgnoreCase(k.getKey())) {
                elements = k.getValue();
                break;
            }
        }
        return elements.size() == 0 ? Optional.empty() : Optional.of(elements.get(0));
    }

    public boolean limited() {
        return retryAfter.isAfter(ZonedDateTime.now());
    }
}
