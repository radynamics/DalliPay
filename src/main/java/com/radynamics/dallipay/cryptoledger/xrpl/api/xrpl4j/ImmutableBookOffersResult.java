package com.radynamics.dallipay.cryptoledger.xrpl.api.xrpl4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import org.xrpl.xrpl4j.model.client.XrplResult;

import java.util.List;
import java.util.Optional;

@JsonDeserialize(as = ImmutableBookOffersResult.class)
public class ImmutableBookOffersResult implements XrplResult {
    private Optional<String> status = Optional.empty();
    private List<ImmutableBookOffersOfferResult> offers = ImmutableList.of();
    private boolean validated;
    boolean validatedIsSet;

    @JsonProperty("status")
    public void setStatus(Optional<String> status) {
        this.status = status;
    }

    @JsonProperty("offers")
    public void setOffers(List<ImmutableBookOffersOfferResult> offers) {
        this.offers = offers;
    }

    @JsonProperty("validated")
    public void setValidated(boolean validated) {
        this.validated = validated;
        this.validatedIsSet = true;
    }

    @Override
    public Optional<String> status() {
        return status;
    }

    public List<ImmutableBookOffersOfferResult> offers() {
        return offers;
    }
}
