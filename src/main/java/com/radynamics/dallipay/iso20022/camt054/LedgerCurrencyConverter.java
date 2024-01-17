package com.radynamics.dallipay.iso20022.camt054;

import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.ExchangeRate;
import com.radynamics.dallipay.exchange.Money;

import java.math.BigDecimal;

public class LedgerCurrencyConverter {
    private final Currency ledgerNativeCcy;
    private final Currency smallestUnitCcy;
    private final Double factor;
    private final LedgerCurrencyFormat defaulTargetFormat;
    private final LedgerCurrencyFormat targetFormat;

    public LedgerCurrencyConverter(Currency ledgerNativeCcy, Currency smallestUnitCcy, long factorToSmallestUnit, LedgerCurrencyFormat defaulTargetFormat, LedgerCurrencyFormat targetFormat) {
        this.ledgerNativeCcy = ledgerNativeCcy;
        this.smallestUnitCcy = smallestUnitCcy;
        this.factor = Double.valueOf(factorToSmallestUnit);
        this.defaulTargetFormat = defaulTargetFormat;
        this.targetFormat = targetFormat;
    }

    public Money convert(Money amount) {
        var amountIsNative = amount.getCcy().sameCode(ledgerNativeCcy);
        var amountIsSmallest = amount.getCcy().sameCode(smallestUnitCcy);
        if (!amountIsNative && !amountIsSmallest) {
            return amount;
        }

        if (amountIsNative && targetFormat == LedgerCurrencyFormat.SmallestUnit) {
            return toSmallestUnit(amount);
        }
        if (amountIsSmallest && targetFormat == LedgerCurrencyFormat.Native) {
            return toNativeUnit(amount);
        }
        return amount;
    }

    private Money toNativeUnit(Money amount) {
        return Money.of(amount.getNumber().doubleValue() / factor, ledgerNativeCcy);
    }

    private Money toSmallestUnit(Money amount) {
        return Money.of(amount.getNumber().doubleValue() * factor, smallestUnitCcy);
    }

    public BigDecimal convert(ExchangeRate exchangeRate) {
        var f = targetFormat == LedgerCurrencyFormat.Native ? 1 : 1 / factor;
        return BigDecimal.valueOf(exchangeRate.getRate() * f);
    }

    public Currency getTargetCurrency(Currency ccy) {
        if (!ccy.sameCode(ledgerNativeCcy)) {
            return ccy;
        }
        return targetFormat == LedgerCurrencyFormat.Native ? ledgerNativeCcy : smallestUnitCcy;
    }

    public Currency getLedgerNativeCcy() {
        return ledgerNativeCcy;
    }

    public Currency getSmallestUnitCcy() {
        return smallestUnitCcy;
    }

    public LedgerCurrencyFormat getDefaultTargetFormat() {
        return defaulTargetFormat;
    }

    public LedgerCurrencyFormat getTargetFormat() {
        return targetFormat;
    }
}
