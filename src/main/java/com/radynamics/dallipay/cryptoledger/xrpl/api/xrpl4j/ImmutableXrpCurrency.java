package com.radynamics.dallipay.cryptoledger.xrpl.api.xrpl4j;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImmutableXrpCurrency {
    @JsonProperty("currency")
    public String currency() {
        return "XRP";
    }
}
