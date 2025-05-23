package com.radynamics.dallipay.iso20022.camt054;

import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.CurrencyConverter;
import com.radynamics.dallipay.exchange.CurrencyPair;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;

import java.util.Arrays;
import java.util.Comparator;

public class ReportBalancesBuilder {
    private final Wallet wallet;
    private Currency targetCcy;
    private Currency ledgerCcy;
    private Payment[] payments;
    private LedgerCurrencyConverter ledgerCurrencyConverter;
    private CurrencyConverter currencyConverter;

    public ReportBalancesBuilder(Wallet wallet) {
        this.wallet = wallet;
    }

    public ReportBalances build() {
        if (payments == null || payments.length == 0) {
            return ReportBalances.Empty;
        }

        var latest = Arrays.stream(payments).max(Comparator.comparing(Payment::getBooked)).orElseThrow();

        var b = ReportBalances.create();
        if (targetCcy == null) {
            // Export in the same ccy as other native amounts.
            b.addClbd(ledgerCurrencyConverter.convert(wallet.getBalances().get(ledgerCcy).orElse(Money.zero(ledgerCcy))));
            b.setClbdAt(latest.getBooked());
            return b;
        }

        // Someone exporting in eg. USD due his accounting software expects amounts in USD. Therefore convert balance into exporting currency.
        if (currencyConverter.has(new CurrencyPair(ledgerCcy, targetCcy))) {
            b.addClbd(currencyConverter.convertMoney(wallet.getBalances().get(ledgerCcy).orElse(Money.zero(ledgerCcy)), targetCcy));
            b.setClbdAt(latest.getBooked());
        }
        return b;
    }

    public static ReportBalancesBuilder create(Wallet wallet) {
        return new ReportBalancesBuilder(wallet);
    }

    public ReportBalancesBuilder targetCcy(Currency targetCcy) {
        this.targetCcy = targetCcy;
        return this;
    }

    public ReportBalancesBuilder ledgerCcy(Currency ledgerCcy) {
        this.ledgerCcy = ledgerCcy;
        return this;
    }

    public ReportBalancesBuilder payments(Payment[] payments) {
        this.payments = payments;
        return this;
    }

    public ReportBalancesBuilder ledgerCurrencyConverter(LedgerCurrencyConverter ledgerCurrencyConverter) {
        this.ledgerCurrencyConverter = ledgerCurrencyConverter;
        return this;
    }

    public ReportBalancesBuilder currencyConverter(CurrencyConverter currencyConverter) {
        this.currencyConverter = currencyConverter;
        return this;
    }
}
