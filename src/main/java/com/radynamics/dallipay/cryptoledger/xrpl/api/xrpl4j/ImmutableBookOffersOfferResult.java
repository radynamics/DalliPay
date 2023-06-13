package com.radynamics.dallipay.cryptoledger.xrpl.api.xrpl4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;

@JsonDeserialize(as = ImmutableBookOffersOfferResult.class)
public class ImmutableBookOffersOfferResult {
    private CurrencyAmount takerGets;
    private CurrencyAmount takerPays;

    public CurrencyAmount takerGets() {
        return takerGets;
    }

    @JsonProperty("TakerGets")
    public void setTakerGets(CurrencyAmount takerGets) {
        this.takerGets = takerGets;
    }

    public CurrencyAmount takerPays() {
        return takerPays;
    }

    @JsonProperty("TakerPays")
    public void setTakerPays(CurrencyAmount takerPays) {
        this.takerPays = takerPays;
    }
}
