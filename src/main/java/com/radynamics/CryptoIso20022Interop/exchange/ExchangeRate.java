package com.radynamics.CryptoIso20022Interop.exchange;

import java.time.LocalDateTime;

public class ExchangeRate {
    private CurrencyPair pair;
    private double rate;
    private LocalDateTime pointInTime;

    public ExchangeRate(String ccyFrom, String ccyTo, double rate, LocalDateTime pointInTime) {
        this(new CurrencyPair(ccyFrom, ccyTo), rate, pointInTime);
    }

    public ExchangeRate(CurrencyPair pair, double rate, LocalDateTime pointInTime) {
        this.pair = pair;
        this.rate = rate;
        this.pointInTime = pointInTime;
    }

    public static ExchangeRate None(String ccy) {
        return new ExchangeRate(new CurrencyPair(ccy, ccy), 1, LocalDateTime.now());
    }

    public CurrencyPair getPair() {
        return pair;
    }

    public double getRate() {
        return rate;
    }

    public LocalDateTime getPointInTime() {
        return pointInTime;
    }
}
