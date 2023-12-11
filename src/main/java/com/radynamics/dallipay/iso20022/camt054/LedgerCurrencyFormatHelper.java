package com.radynamics.dallipay.iso20022.camt054;

import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;

public final class LedgerCurrencyFormatHelper {
    private static HashMap<String, LedgerCurrencyFormat> map;

    static {
        map = new HashMap<>();
        map.put("native", LedgerCurrencyFormat.Native);
        map.put("smallestUnit", LedgerCurrencyFormat.SmallestUnit);
    }

    public static String toKey(LedgerCurrencyFormat value) {
        for (var o : map.entrySet()) {
            if (o.getValue() == value) {
                return o.getKey();
            }
        }
        throw new NotImplementedException(String.format("Value %s unknown.", value));
    }

    public static LedgerCurrencyFormat toType(String value) {
        return map.get(value);
    }

    public static LedgerCurrencyFormat[] all() {
        return map.values().toArray(new LedgerCurrencyFormat[0]);
    }
}
