package com.radynamics.CryptoIso20022Interop.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class Utils {
    public static final NumberFormat createFormatFiat() {
        var df = DecimalFormat.getInstance();
        setDigits(df, 2);
        return df;
    }

    public static final NumberFormat createFormatLedger() {
        var df = DecimalFormat.getInstance();
        setDigits(df, 6);
        return df;
    }

    private static void setDigits(NumberFormat df, int digits) {
        df.setMinimumFractionDigits(digits);
        df.setMaximumFractionDigits(digits);
    }
}