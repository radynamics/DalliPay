package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.MoneyFormatter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class MoneyBagFormatter {
    public static String format(MoneyBag bag) {
        var map = new HashMap<String, Double>();
        for (var amt : bag.all()) {
            map.put(amt.getCcy().getCode(), amt.getNumber().doubleValue());
        }
        return format(map);
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
