package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.MoneyFormatter;

import java.math.BigDecimal;
import java.util.Map;

public class MoneyBagFormatter {
    public static String format(MoneyBag bag) {
        return format(bag.all());
    }

    public static String format(Map<String, Double> bag) {
        var sb = new StringBuilder();
        var i = 0;
        for (var sum : bag.entrySet()) {
            sb.append(MoneyFormatter.formatFiat(BigDecimal.valueOf(sum.getValue()), sum.getKey()));
            if (i + 1 < bag.size()) {
                sb.append(", ");
            }
            i++;
        }
        return sb.toString();
    }
}
