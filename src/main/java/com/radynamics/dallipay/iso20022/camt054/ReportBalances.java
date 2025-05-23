package com.radynamics.dallipay.iso20022.camt054;

import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;

import java.time.ZonedDateTime;
import java.util.Optional;

public class ReportBalances {
    private Money opbd;
    private ZonedDateTime opbdAt;
    private Money clbd;
    private ZonedDateTime clbdAt;

    public static final ReportBalances Empty = new ReportBalances();

    private ReportBalances() {
    }

    public static ReportBalances create() {
        return new ReportBalances();
    }

    public Optional<Money> getOpbd(Currency ccy) {
        return get(opbd, ccy);
    }

    public void addOpbd(Money opbd) {
        this.opbd = opbd;
    }

    private static Optional<Money> get(Money value, Currency ccy) {
        if (value == null) return Optional.empty();
        if (!value.getCcy().sameCode(ccy)) return Optional.empty();
        return Optional.of(value);
    }

    public Optional<Money> getClbd(Currency ccy) {
        return get(clbd, ccy);
    }

    public void addClbd(Money clbd) {
        this.clbd = clbd;
    }

    public ZonedDateTime getClbdAt() {
        return clbdAt;
    }

    public void setClbdAt(ZonedDateTime clbdAt) {
        this.clbdAt = clbdAt;
    }

    public ZonedDateTime getOpbdAt() {
        return opbdAt;
    }

    public void setOpbdAt(ZonedDateTime opbdAt) {
        this.opbdAt = opbdAt;
    }
}
