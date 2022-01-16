package com.radynamics.CryptoIso20022Interop.exchange;

public class ExchangeRate {
    private String ccyFrom;
    private String ccyTo;
    private double rate;

    public ExchangeRate(String ccyFrom, String ccyTo, double rate) {
        this.ccyFrom = ccyFrom;
        this.ccyTo = ccyTo;
        this.rate = rate;
    }

    public ExchangeRate(CurrencyPair pair, double rate) {
        this(pair.getFirst(), pair.getSecond(), rate);
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
}
