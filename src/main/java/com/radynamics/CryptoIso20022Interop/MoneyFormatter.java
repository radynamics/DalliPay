package com.radynamics.CryptoIso20022Interop;

import com.radynamics.CryptoIso20022Interop.cryptoledger.MoneySums;
import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.ui.Utils;

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

    public static String formatFiat(BigDecimal amount, String ccy) {
        return formatFiat(Utils.createFormatFiat().format(amount), ccy);
    }

    public static String formatFiat(String amount, String ccy) {
        return String.format("%s %s", amount, ccy);
    }

    public static String formatLedger(MoneySums sums) {
        var sb = new StringBuilder();
        var allSums = sums.sum();
        for (var sum : allSums) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(formatLedger(sum));
        }
        return sb.toString();
    }
}
