package com.radynamics.dallipay.iso20022;

import com.radynamics.dallipay.MoneyFormatter;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.ui.Utils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class AmountFormatter {
    private static final NumberFormat dfFiat = Utils.createFormatFiat();
    private static final NumberFormat dfCryptocurrency = Utils.createFormatLedger();

    public static String formatAmt(Payment p) {
        if (p.isAmountUnknown()) {
            return "n/a";
        }
        if (p.getAmount() == null) {
            return "...";
        }

        var df = StringUtils.equalsIgnoreCase(p.getLedger().getNativeCcySymbol(), p.getUserCcyCodeOrEmpty())
                ? dfCryptocurrency
                : dfFiat;
        return df.format(p.getAmount());
    }

    public static String formatAmtWithCcyFiat(Money amt) {
        return formatAmtWithCcy(dfFiat, amt);
    }

    public static String formatAmtWithCcyLedgerCcy(Money amt) {
        return formatAmtWithCcy(dfCryptocurrency, amt);
    }

    private static String formatAmtWithCcy(NumberFormat nf, Money amt) {
        if (Payment.isAmountUnknown(amt)) {
            return "n/a";
        }
        return MoneyFormatter.formatFiat(nf.format(amt.getNumber()), amt.getCcy().getCode());
    }

    public static String formatAmtWithCcy(Payment p) {
        return p.isAmountUnknown() || p.getAmount() == null
                ? MoneyFormatter.formatFiat(formatAmt(p), p.getUserCcyCodeOrEmpty())
                : MoneyFormatter.formatFiat(BigDecimal.valueOf(p.getAmount()), p.getUserCcyCodeOrEmpty());
    }
}
