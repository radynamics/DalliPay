package com.radynamics.dallipay.iso20022;

import com.radynamics.dallipay.exchange.ExchangeRate;

public class ExchangeRateFormatter {
    public static String format(ExchangeRate rate) {
        return "%s %s".formatted(rate.getRate(), rate.getPair().getDisplayText());
    }
}
