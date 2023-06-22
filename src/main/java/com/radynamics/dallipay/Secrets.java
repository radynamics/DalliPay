package com.radynamics.dallipay;

import com.radynamics.dallipay.db.ConfigRepo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Secrets {
    private final static Logger log = LogManager.getLogger(Secrets.class);

    /**
     * Return Xumm API-Key from Xumm Developer Console (https://apps.xumm.dev) or null if none is available.
     */
    public static String getXummApiKey() {
        var fallback = SecretsProd.getXummApiKey();
        try (var repo = new ConfigRepo()) {
            return repo.getApiKeyXumm().orElse(fallback);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return fallback;
        }
    }
}
