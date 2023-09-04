package com.radynamics.dallipay.cryptoledger.xrpl.api.xrpl4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.xrpl.xrpl4j.model.transactions.Address;

public class ImmutableIssuedCurrency {
    private String currency;
    private Address issuer;

    public static ImmutableIssuedCurrency of(String currency, Address issuer) {
        var o = new ImmutableIssuedCurrency();
        o.currency = currency;
        o.issuer = issuer;
        return o;
    }

    @JsonProperty("currency")
    public String currency() {
        return currency;
    }

    @JsonProperty("issuer")
    public Address issuer() {
        return issuer;
    }
}
