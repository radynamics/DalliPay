package com.radynamics.CryptoIso20022Interop.exchange;

import java.math.BigDecimal;

public class CurrencyConverter {
    private final ExchangeRate[] rates;

    public CurrencyConverter(ExchangeRate[] rates) {
        this.rates = rates;
    }

    public double convert(BigDecimal amount, String sourceCcy, String targetCcy) {
        if (sourceCcy.equalsIgnoreCase(targetCcy)) {
            return amount.doubleValue();
        }

        for (var r : rates) {
            if (r.getCcyFrom().equalsIgnoreCase(sourceCcy) && r.getCcyTo().equalsIgnoreCase(targetCcy)) {
                return amount.doubleValue() * r.getRate();
            }
            if (r.getCcyFrom().equalsIgnoreCase(targetCcy) && r.getCcyTo().equalsIgnoreCase(sourceCcy)) {
                return amount.doubleValue() / r.getRate();
            }
        }

        throw new RuntimeException(String.format("No exchange for %s/%s available.", sourceCcy, targetCcy));
    }
}
