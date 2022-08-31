package com.radynamics.CryptoIso20022Interop.exchange;

import java.util.Objects;

public class Money {
    private final Number number;
    private final Currency ccy;

    private Money(Number number, Currency ccy) {
        if (number == null) throw new IllegalArgumentException("Parameter 'number' cannot be null");
        if (ccy == null) throw new IllegalArgumentException("Parameter 'ccy' cannot be null");
        this.number = number;
        this.ccy = ccy;
    }

    public static Money of(Number number, Currency ccy) {
        return new Money(number, ccy);
    }

    public static Money zero(Money value) {
        return zero(value.getCcy());
    }

    public static Money zero(Currency ccy) {
        return Money.of(0, ccy);
    }

    public Money plus(Money value) {
        assertSameCcy(value);
        return Money.of(getNumber().doubleValue() + value.getNumber().doubleValue(), getCcy());
    }

    public Money minus(Money value) {
        assertSameCcy(value);
        return Money.of(getNumber().doubleValue() - value.getNumber().doubleValue(), getCcy());
    }

    public Money multiply(Double value) {
        return Money.of(getNumber().doubleValue() * value, getCcy());
    }

    private void assertSameCcy(Money value) {
        if (value == null) throw new IllegalArgumentException("Parameter 'value' cannot be null");
        if (!value.getCcy().equals(getCcy())) {
            throw new IllegalArgumentException(String.format("Currencies %s and %s must be equal", value.getCcy(), getCcy()));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(number, money.number) && Objects.equals(ccy, money.ccy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, ccy);
    }

    public Number getNumber() {
        return number;
    }

    public Currency getCcy() {
        return ccy;
    }

    @Override
    public String toString() {
        return String.format("{%s %s}", number, ccy);
    }
}
