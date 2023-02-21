package com.radynamics.dallipay;

import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.ui.Utils;

import java.math.BigDecimal;

public class MoneyFormatter {
    public static String formatLedger(Double amount, String ccy) {
        return formatLedger(BigDecimal.valueOf(amount), ccy);
    }

    public static String formatLedger(BigDecimal amount, String ccy) {
        return formatLedger(Money.of(amount, new Currency(ccy)));
    }

    public static String formatLedger(Money amount) {
        return String.format("%s %s", Utils.createFormatLedger().format(amount.getNumber()), amount.getCcy().getCode());
    }

    public static String formatExact(Money amount) {
        return formatFiat(amount.getNumber().toString(), amount.getCcy().getCode());
    }

    public static String formatFiat(Money amount) {
        return formatFiat(Utils.createFormatFiat().format(amount.getNumber()), amount.getCcy().getCode());
    }

    public static String formatFiat(BigDecimal amount, String ccy) {
        return formatFiat(Utils.createFormatFiat().format(amount), ccy);
    }

    public static String formatFiat(String amount, String ccy) {
        return String.format("%s %s", amount, ccy);
    }

    public static String formatFiat(Money[] amounts) {
        return formatFiat(amounts, ", ");
    }

    public static String formatFiat(Money[] amounts, String delimiter) {
        var sb = new StringBuilder();
        var i = 0;
        for (var amt : amounts) {
            sb.append(MoneyFormatter.formatFiat(amt));
            if (i + 1 < amounts.length) {
                sb.append(delimiter);
            }
            i++;
        }
        return sb.toString();
    }
}
