package com.radynamics.dallipay;

public final class Secrets {
    /**
     * Return Xumm API-Key from Xumm Developer Console (https://apps.xumm.dev) or null if none is available.
     */
    public static String getXummApiKey() {
        return SecretsProd.getXummApiKey();
    }
}