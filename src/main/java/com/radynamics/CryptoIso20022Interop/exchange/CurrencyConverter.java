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
            if (r.getPair().getFirst().equalsIgnoreCase(sourceCcy) && r.getPair().getSecond().equalsIgnoreCase(targetCcy)) {
                return Math.round(amount.doubleValue() * r.getRate() * PRECISION) / PRECISION;
            }
            if (r.getPair().getFirst().equalsIgnoreCase(targetCcy) && r.getPair().getSecond().equalsIgnoreCase(sourceCcy)) {
                return Math.round(amount.doubleValue() / r.getRate() * PRECISION) / PRECISION;
            }
        }

        throw new RuntimeException(String.format("No exchange rate for %s/%s available.", sourceCcy, targetCcy));
    }

    public boolean has(CurrencyPair pair) {
        if (pair == null) throw new IllegalArgumentException("Parameter 'pair' cannot be null");

        for (var r : rates) {
            var matchesFrom = r.getPair().getFirst().equalsIgnoreCase(pair.getFirst()) || r.getPair().getFirst().equalsIgnoreCase(pair.getSecond());
            var matchesTo = r.getPair().getSecond().equalsIgnoreCase(pair.getFirst()) || r.getPair().getSecond().equalsIgnoreCase(pair.getSecond());
            if (matchesFrom && matchesTo) {
                return true;
            }
        }
        return false;
    }
}
