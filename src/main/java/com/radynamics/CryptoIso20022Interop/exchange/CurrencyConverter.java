package com.radynamics.CryptoIso20022Interop.exchange;

import java.math.BigDecimal;

public class CurrencyConverter {
    private final ExchangeRate[] rates;

    public static final double PRECISION = 100000d;

    public CurrencyConverter() {
        this(new ExchangeRate[0]);
    }

    public CurrencyConverter(ExchangeRate[] rates) {
        this.rates = rates;
    }

    public double convert(BigDecimal amount, CurrencyPair pair) {
        return convert(amount, pair.getFirst(), pair.getSecond());
    }

    public double convert(BigDecimal amount, String sourceCcy, String targetCcy) {
        if (sourceCcy == null) throw new IllegalArgumentException("Parameter 'sourceCcy' cannot be null");
        if (sourceCcy.length() == 0) throw new IllegalArgumentException("Parameter 'sourceCcy' cannot be empty");
        if (targetCcy == null) throw new IllegalArgumentException("Parameter 'targetCcy' cannot be null");
        if (targetCcy.length() == 0) throw new IllegalArgumentException("Parameter 'targetCcy' cannot be empty");
        if (sourceCcy.equalsIgnoreCase(targetCcy)) {
            return amount.doubleValue();
        }

        for (var r : rates) {
            if (r.getCcyFrom().equalsIgnoreCase(sourceCcy) && r.getCcyTo().equalsIgnoreCase(targetCcy)) {
                return Math.round(amount.doubleValue() * r.getRate() * PRECISION) / PRECISION;
            }
            if (r.getCcyFrom().equalsIgnoreCase(targetCcy) && r.getCcyTo().equalsIgnoreCase(sourceCcy)) {
                return Math.round(amount.doubleValue() / r.getRate() * PRECISION) / PRECISION;
            }
        }

        throw new RuntimeException(String.format("No exchange rate for %s/%s available.", sourceCcy, targetCcy));
    }

    public boolean has(CurrencyPair pair) {
        if (pair == null) throw new IllegalArgumentException("Parameter 'pair' cannot be null");

        for (var r : rates) {
            var matchesFrom = r.getCcyFrom().equalsIgnoreCase(pair.getFirst()) || r.getCcyFrom().equalsIgnoreCase(pair.getSecond());
            var matchesTo = r.getCcyTo().equalsIgnoreCase(pair.getFirst()) || r.getCcyTo().equalsIgnoreCase(pair.getSecond());
            if (matchesFrom && matchesTo) {
                return true;
            }
        }
        return false;
    }
}
