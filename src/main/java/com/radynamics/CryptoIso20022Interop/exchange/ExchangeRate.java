package com.radynamics.CryptoIso20022Interop.exchange;

import java.time.ZonedDateTime;

public class ExchangeRate {
    private CurrencyPair pair;
    private double rate;
    private ZonedDateTime pointInTime;

    public static final double UndefinedRate = 0;

    public ExchangeRate(String ccyFrom, String ccyTo, double rate, ZonedDateTime pointInTime) {
        this(new CurrencyPair(ccyFrom, ccyTo), rate, pointInTime);
    }

    public ExchangeRate(CurrencyPair pair, double rate, ZonedDateTime pointInTime) {
        this.pair = pair;
        setRate(rate);
        this.pointInTime = pointInTime;
    }

    public static ExchangeRate None(String ccy) {
        return OneToOne(new CurrencyPair(ccy, ccy));
    }

    public static ExchangeRate OneToOne(CurrencyPair pair) {
        return new ExchangeRate(pair.getFirst(), pair.getSecond(), 1, ZonedDateTime.now());
    }

    public static ExchangeRate Undefined(CurrencyPair pair) {
        return new ExchangeRate(pair, UndefinedRate, ZonedDateTime.now());
    }

    public static ExchangeRate getOrNull(ExchangeRate[] rates, CurrencyPair pair) {
        if (rates == null) throw new IllegalArgumentException("Parameter 'rates' cannot be null");
        for (var r : rates) {
            if (r.getPair().sameAs(pair)) {
                return r;
            }
        }
        return null;
    }

    public boolean isUndefined() {
        return getRate() == UndefinedRate;
    }

    public boolean isNone() {
        return getPair().getFirst().equals(getPair().getSecond()) && getRate() == 1;
    }

    public CurrencyPair getPair() {
        return pair;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        if (rate < 0) throw new IllegalArgumentException("Parameter 'rates' cannot be less than zero");
        this.rate = rate;
    }

    public ZonedDateTime getPointInTime() {
        return pointInTime;
    }

    @Override
    public String toString() {
        return String.format("pair=%s, rate=%s", pair, rate);
    }
}
