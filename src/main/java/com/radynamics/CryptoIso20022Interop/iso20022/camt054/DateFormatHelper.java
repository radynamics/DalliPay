package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;

public final class DateFormatHelper {
    private static HashMap<String, DateFormat> map;

    static {
        map = new HashMap<>();
        map.put("date", DateFormat.Date);
        map.put("datetime", DateFormat.DateTime);
    }

    public static String toKey(DateFormat value) {
        for (var o : map.entrySet()) {
            if (o.getValue() == value) {
                return o.getKey();
            }
        }
        throw new NotImplementedException(String.format("Value %s unknown.", value));
    }

    public static DateFormat toType(String value) {
        return map.get(value);
    }
}
