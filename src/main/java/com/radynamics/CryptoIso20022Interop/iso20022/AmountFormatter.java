package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.ui.Utils;
import org.apache.commons.lang3.StringUtils;

import java.text.NumberFormat;

public class AmountFormatter {
    private static final NumberFormat dfFiat = com.radynamics.CryptoIso20022Interop.ui.Utils.createFormatFiat();
    private static final NumberFormat dfCryptocurrency = Utils.createFormatFiat();

    public static String formatAmt(Payment p) {
        if (p.isAmountUnknown()) {
            return "n/a";
        }
        if (p.getAmount() == null) {
            return "...";
        }

        var df = StringUtils.equalsIgnoreCase(p.getLedger().getNativeCcySymbol(), p.getFiatCcy())
                ? dfCryptocurrency
                : dfFiat;
        return df.format(p.getAmount());
    }
}
