package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AmountRounder {
    public static BigDecimal round(double value, int digits) {
        var v = BigDecimal.valueOf(value);
        return v.setScale(digits, RoundingMode.HALF_UP);
    }
}
