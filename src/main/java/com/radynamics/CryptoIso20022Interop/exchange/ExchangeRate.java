package com.radynamics.CryptoIso20022Interop.exchange;

import java.time.LocalDateTime;

public class ExchangeRate {
    private String ccyFrom;
    private String ccyTo;
    private double rate;
    private LocalDateTime pointInTime;

    public ExchangeRate(String ccyFrom, String ccyTo, double rate, LocalDateTime pointInTime) {
        this.ccyFrom = ccyFrom;
        this.ccyTo = ccyTo;
        this.rate = rate;
        this.pointInTime = pointInTime;
    }

    public ExchangeRate(CurrencyPair pair, double rate, LocalDateTime pointInTime) {
        this(pair.getFirst(), pair.getSecond(), rate, pointInTime);
    }

    public String getCcyFrom() {
        return ccyFrom;
    }

    public String getCcyTo() {
        return ccyTo;
    }

    public double getRate() {
        return rate;
    }

    public LocalDateTime getPointInTime() {
        return pointInTime;
    }
}
