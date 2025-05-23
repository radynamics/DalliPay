package com.radynamics.dallipay.exchange;

import java.math.BigDecimal;

public class CurrencyConverter {
    private final ExchangeRate[] rates;

    public static final double PRECISION = 1000000000d;

    public CurrencyConverter() {
        this(new ExchangeRate[0]);
    }

    public CurrencyConverter(ExchangeRate[] rates) {
        this.rates = rates;
    }

    public Money convertMoney(Money amount, Currency targetCcy) {
        return Money.of(convert(amount, targetCcy), targetCcy);
    }

    public double convert(Money amount, Currency targetCcy) {
        return convert(BigDecimal.valueOf(amount.getNumber().doubleValue()), amount.getCcy().getCode(), targetCcy.getCode());
    }

    public double convert(BigDecimal amount, CurrencyPair pair) {
        return convert(amount, pair.getFirstCode(), pair.getSecondCode());
    }

    public double convert(BigDecimal amount, String sourceCcy, String targetCcy) {
        if (sourceCcy == null) throw new IllegalArgumentException("Parameter 'sourceCcy' cannot be null");
        if (sourceCcy.length() == 0) throw new IllegalArgumentException("Parameter 'sourceCcy' cannot be empty");
        if (targetCcy == null) throw new IllegalArgumentException("Parameter 'targetCcy' cannot be null");
        if (targetCcy.length() == 0) throw new IllegalArgumentException("Parameter 'targetCcy' cannot be empty");
        if (sourceCcy.equalsIgnoreCase(targetCcy)) {
            return amount.doubleValue();
        }

        // TODO: improve rounding (ex. JPY)
        for (var r : rates) {
            if (r.getPair().getFirstCode().equalsIgnoreCase(sourceCcy) && r.getPair().getSecondCode().equalsIgnoreCase(targetCcy)) {
                return Math.round(amount.doubleValue() * r.getRate() * PRECISION) / PRECISION;
            }
            if (r.getPair().getFirstCode().equalsIgnoreCase(targetCcy) && r.getPair().getSecondCode().equalsIgnoreCase(sourceCcy)) {
                return Math.round(amount.doubleValue() / r.getRate() * PRECISION) / PRECISION;
            }
        }

        throw new RuntimeException(String.format("No exchange rate for %s/%s available.", sourceCcy, targetCcy));
    }

    public boolean has(CurrencyPair pair) {
        return get(pair) != null;
    }

    public ExchangeRate get(CurrencyPair pair) {
        if (pair == null) throw new IllegalArgumentException("Parameter 'pair' cannot be null");

        for (var r : rates) {
            var matches = r.getPair().getFirstCode().equalsIgnoreCase(pair.getFirstCode()) && r.getPair().getSecondCode().equalsIgnoreCase(pair.getSecondCode());
            var matchesInverted = r.getPair().getFirstCode().equalsIgnoreCase(pair.getSecondCode()) && r.getPair().getSecondCode().equalsIgnoreCase(pair.getFirstCode());
            if (matches || matchesInverted) {
                return r;
            }
        }
        return null;
    }
}
