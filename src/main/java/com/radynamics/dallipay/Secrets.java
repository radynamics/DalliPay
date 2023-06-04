package com.radynamics.dallipay;

public final class Secrets {
    /**
     * Return Xumm API-Key from Xumm Developer Console (https://apps.xumm.dev) or null if none is available.
     */
    public static String getXummApiKey() {
        return SecretsProd.getXummApiKey();
    }

    /**
     * Return Alchemy API-Key for Ethereum Mainnet.
     */
    public static String getAlchemyApiKeyEthereumMainnnet() {
        return SecretsProd.getAlchemyApiKeyEthereumMainnnet();
    }

    /**
     * Return Alchemy API-Key for Ethereum Goerli.
     */
    public static String getAlchemyApiKeyEthereumGoerli() {
        return SecretsProd.getAlchemyApiKeyEthereumGoerli();
    }
}
