package com.radynamics.dallipay.cryptoledger.xrpl.api.xrpl4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.xrpl.xrpl4j.client.JsonRpcRequest;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.transactions.Address;

@JsonSerialize(as = ImmutableBookOffersRequestParams.class)
public class ImmutableBookOffersRequestParams implements XrplRequestParams {
    private final Address taker;
    private final ImmutableXrpCurrency takerGets;
    private final ImmutableIssuedCurrency takerPays;
    private final int limit;

    public ImmutableBookOffersRequestParams(Builder builder) {
        this.taker = builder.taker;
        this.takerGets = builder.takerGets;
        this.takerPays = builder.takerPays;
        this.limit = builder.limit;
    }

    @JsonProperty("taker")
    public Address taker() {
        return taker;
    }

    @JsonProperty("taker_gets")
    public ImmutableXrpCurrency takerGets() {
        return takerGets;
    }

    @JsonProperty("taker_pays")
    public ImmutableIssuedCurrency takerPays() {
        return takerPays;
    }

    @JsonProperty("limit")
    public int limit() {
        return limit;
    }

    public JsonRpcRequest request() {
        return JsonRpcRequest.builder()
                .method("book_offers")
                .addParams(this)
                .build();
    }

    public static ImmutableBookOffersRequestParams.Builder builder() {
        return new ImmutableBookOffersRequestParams.Builder();
    }

    public static final class Builder {
        private Address taker;
        private ImmutableXrpCurrency takerGets;
        private ImmutableIssuedCurrency takerPays;
        private int limit = 10;

        public Builder taker(Address taker) {
            this.taker = taker;
            return this;
        }

        public Builder takerGets(ImmutableXrpCurrency currency) {
            this.takerGets = currency;
            return this;
        }

        public Builder takerPays(ImmutableIssuedCurrency currency) {
            this.takerPays = currency;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public ImmutableBookOffersRequestParams build() {
            return new ImmutableBookOffersRequestParams(this);
        }
    }
}
