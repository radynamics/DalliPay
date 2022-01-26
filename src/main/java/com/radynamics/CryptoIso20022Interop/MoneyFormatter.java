package com.radynamics.CryptoIso20022Interop;

import com.radynamics.CryptoIso20022Interop.ui.Utils;

import java.math.BigDecimal;

public class MoneyFormatter {
    public static final String formatLedger(BigDecimal amount, String ccy) {
        return String.format("%s %s", Utils.createFormatLedger().format(amount), ccy);
    }

    public static final String formatFiat(BigDecimal amount, String ccy) {
        return formatFiat(Utils.createFormatFiat().format(amount), ccy);
    }

    public static final String formatFiat(String amount, String ccy) {
        return String.format("%s %s", Utils.createFormatFiat().format(amount), ccy);
    }
}
