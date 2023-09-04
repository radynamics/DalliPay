package com.radynamics.dallipay.cryptoledger.xrpl.paystring;

public class Address {
    private String paymentNetwork;
    private String environment;
    private CryptoAddressDetails details;

    public String getPaymentNetwork() {
        return paymentNetwork;
    }

    public void setPaymentNetwork(String paymentNetwork) {
        this.paymentNetwork = paymentNetwork;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setDetails(CryptoAddressDetails details) {
        this.details = details;
    }

    public CryptoAddressDetails getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "net: %s, env: %s".formatted(paymentNetwork, environment);
    }
}
