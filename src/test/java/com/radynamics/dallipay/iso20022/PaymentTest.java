package com.radynamics.dallipay.iso20022;

import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.CurrencyPair;
import com.radynamics.dallipay.exchange.ExchangeRate;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.pain001.Assertion;
import com.radynamics.dallipay.iso20022.pain001.TestLedger;
import com.radynamics.dallipay.iso20022.pain001.TestTransaction;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.ZonedDateTime;

public class PaymentTest {
    @Test
    public void ctrNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Payment(null);
        });
    }

    @Test
    public void getAmountCcy() {
        var p = new Payment(new TestTransaction(new TestLedger(), 10.0, "TEST"));
        p.setExchangeRate(createRate2());

        Assertions.assertEquals(10.0, p.getAmountTransaction().getNumber());
        Assertions.assertEquals("TEST", p.getAmountTransaction().getCcy().getCode());
        Assertions.assertEquals(20, p.getAmount());
        Assertions.assertEquals("USD", p.getUserCcyCodeOrEmpty());
    }

    @NotNull
    private ExchangeRate createRate2() {
        return new ExchangeRate(new CurrencyPair("TEST", "USD"), 2.0, ZonedDateTime.now());
    }

    @Test
    public void getAmountNoExchangeRate() {
        var p = new Payment(new TestTransaction(new TestLedger(), 10.0, "TEST"));

        Assertions.assertTrue(p.isAmountUnknown());
        Assertions.assertTrue(p.isCcyUnknown());
        Assertions.assertEquals(0, p.getAmount());
        Assertions.assertEquals("TEST", p.getAmountTransaction().getCcy().getCode());
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null", "null,''", "10,null"}, nullValues = {"null"})
    public void setAmountNull(Double amt, String ccy) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            var p = new Payment(new TestTransaction(new TestLedger(), 10.0, "TEST"));
            p.setAmount(amt == null ? null : Money.of(amt, new Currency(ccy)));
        });
    }

    @Test
    public void setAmount() {
        var p = new Payment(new TestTransaction(new TestLedger(), 10.0, "TEST"));
        p.setExchangeRate(createRate2());
        p.setAmount(Money.of(30d, new Currency("USD")));

        Assertions.assertEquals(30, p.getAmount());
        Assertions.assertEquals("USD", p.getUserCcyCodeOrEmpty());
        Assertions.assertEquals(15.0, p.getAmountTransaction().getNumber());
        Assertions.assertEquals("TEST", p.getAmountTransaction().getCcy().getCode());
    }

    @Test
    public void setAmountIssuedCcy() {
        var ledger = new TestLedger();
        var ccyAAA = TestUtils.createIssuedCcy(ledger, "AAA");
        var p = new Payment(new TestTransaction(ledger, Money.of(10d, ccyAAA)));
        p.setExchangeRate(null);
        p.setAmount(Money.of(10d, ccyAAA));

        Assertions.assertEquals(10, p.getAmount());
        Assertions.assertEquals("AAA", p.getUserCcyCodeOrEmpty());
        Assertion.assertEquals(Money.of(10.0, ccyAAA), p.getAmountTransaction());
        Assertions.assertNull(p.getExchangeRate());
        Assertions.assertFalse(p.isAmountUnknown());
    }

    @Test
    public void setAmountNoExchangeRate() {
        var p = new Payment(new TestTransaction(new TestLedger(), 10.0, "TEST"));
        p.setAmount(Money.of(30d, new Currency("USD")));

        Assertions.assertEquals(30, p.getAmount());
        Assertions.assertEquals("USD", p.getUserCcyCodeOrEmpty());
        Assertions.assertEquals(0d, p.getAmountTransaction().getNumber());
        Assertions.assertEquals("TEST", p.getAmountTransaction().getCcy().getCode());
    }

    @ParameterizedTest
    @CsvSource(value = {"USD,TEST", "TEST,USD"})
    public void setExchangeRate(String ccyFrom, String ccyTo) {
        var p = new Payment(new TestTransaction(new TestLedger(), 10.0, "TEST"));
        p.setExchangeRate(new ExchangeRate(new CurrencyPair(ccyFrom, ccyTo), 2.0, ZonedDateTime.now()));

        Assertions.assertEquals(20, p.getAmount());
        Assertions.assertEquals("USD", p.getUserCcyCodeOrEmpty());
    }

    @ParameterizedTest
    @CsvSource(value = {"true", "false"})
    public void setExchangeRateNull(boolean amountDefined) {
        var p = new Payment(new TestTransaction(new TestLedger(), 10.0, "TEST"));
        p.setExchangeRate(new ExchangeRate(new CurrencyPair("USD", "TEST"), 2.0, ZonedDateTime.now()));
        if (amountDefined) {
            p.setAmount(Money.of(20d, new Currency("USD")));

        }
        Assertions.assertEquals(20, p.getAmount());
        Assertions.assertEquals("USD", p.getUserCcyCodeOrEmpty());
        Assertions.assertEquals(10.0, p.getAmountTransaction().getNumber());

        p.setExchangeRate(null);

        Assertions.assertEquals(amountDefined ? 20 : 0, p.getAmount());
        Assertions.assertEquals("USD", p.getUserCcyCodeOrEmpty());
        Assertions.assertEquals(amountDefined ? 0 : 10.0, p.getAmountTransaction().getNumber().doubleValue());
    }

    @ParameterizedTest
    @CsvSource(value = {"XRP,BTC", "XRP,BTC"})
    public void setExchangeRateLedgerCcyNotAffected(String ccyFrom, String ccyTo) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            var p = new Payment(new TestTransaction(new TestLedger(), 10.0, "TEST"));
            p.setExchangeRate(new ExchangeRate(new CurrencyPair(ccyFrom, ccyTo), 2.0, ZonedDateTime.now()));
        });
    }

    @ParameterizedTest
    @CsvSource(value = {"XRP,BTC", "XRP,TEST", "TEST,XRP"})
    public void setExchangeRateFiatCcyNotAffected(String ccyFrom, String ccyTo) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            var p = new Payment(new TestTransaction(new TestLedger(), 10.0, "TEST"));
            p.setAmount(Money.of(30d, new Currency("USD")));
            p.setExchangeRate(new ExchangeRate(new CurrencyPair(ccyFrom, ccyTo), 2.0, ZonedDateTime.now()));
        });
    }
}
