package com.radynamics.dallipay.iso20022.camt054;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.exchange.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AmountRounder {
    public static BigDecimal round(Ledger ledger, Money value, int digits) {
        var round = !ledger.getNativeCcySymbol().equals(value.getCcy().getCode());
        var rounded = AmountRounder.round(value.getNumber().doubleValue(), digits);
        if (round) {
            return rounded;
        } else {
            // Ensure a value like "36.35" is returned as "36.3500"
            var exact = BigDecimal.valueOf(value.getNumber().doubleValue());
            return rounded.subtract(exact).doubleValue() == 0 ? rounded : exact;
        }
    }

    public static BigDecimal round(double value, int digits) {
        var v = BigDecimal.valueOf(value);
        return v.setScale(digits, RoundingMode.HALF_UP);
    }
}
