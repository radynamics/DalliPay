package com.radynamics.dallipay.cryptoledger.generic;

import com.radynamics.dallipay.cryptoledger.Key;
import com.radynamics.dallipay.exchange.CurrencyPair;

import java.time.ZonedDateTime;

public class RateAtTimeKey implements Key {
    private final CurrencyPair pair;
    private final ZonedDateTime pointInTime;

    public RateAtTimeKey(CurrencyPair pair, ZonedDateTime pointInTime) {
        if (pair == null) throw new IllegalArgumentException("Parameter 'pair' cannot be null");
        if (pointInTime == null) throw new IllegalArgumentException("Parameter 'pointInTime' cannot be null");
        this.pair = pair;
        this.pointInTime = pointInTime;
    }

    @Override
    public String get() {
        return "%s_%s_%s".formatted(pair.getFirst().getCode(), pair.getSecond().getCode(), pointInTime);
    }
}
