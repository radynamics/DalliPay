package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.exchange.CurrencyPair;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.TestLedger;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.TestTransaction;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
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

        Assertions.assertEquals(10.0, p.getAmountLedgerUnit());
        Assertions.assertEquals("TEST", p.getLedgerCcy());
        Assertions.assertEquals(20, p.getAmount());
        Assertions.assertEquals("USD", p.getFiatCcy());
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
        Assertions.assertEquals("TEST", p.getLedgerCcy());
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null", "null,''", "10,null"}, nullValues = {"null"})
    public void setAmountNull(Double amt, String ccy) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            var p = new Payment(new TestTransaction(new TestLedger(), 10.0, "TEST"));
            p.setAmount(amt == null ? null : BigDecimal.valueOf(amt), ccy);
        });
    }

    @Test
    public void setAmount() {
        var p = new Payment(new TestTransaction(new TestLedger(), 10.0, "TEST"));
        p.setExchangeRate(createRate2());
        p.setAmount(BigDecimal.valueOf(30), "USD");

        Assertions.assertEquals(30, p.getAmount());
        Assertions.assertEquals("USD", p.getFiatCcy());
        Assertions.assertEquals(15.0, p.getAmountLedgerUnit());
        Assertions.assertEquals("TEST", p.getLedgerCcy());
    }

    @Test
    public void setAmountNoExchangeRate() {
        var p = new Payment(new TestTransaction(new TestLedger(), 10.0, "TEST"));
        p.setAmount(BigDecimal.valueOf(30), "USD");

        Assertions.assertEquals(30, p.getAmount());
        Assertions.assertEquals("USD", p.getFiatCcy());
        Assertions.assertEquals(0, p.getAmountLedgerUnit());
        Assertions.assertEquals("TEST", p.getLedgerCcy());
    }

    @ParameterizedTest
    @CsvSource(value = {"USD,TEST", "TEST,USD"})
    public void setExchangeRate(String ccyFrom, String ccyTo) {
        var p = new Payment(new TestTransaction(new TestLedger(), 10.0, "TEST"));
        p.setExchangeRate(new ExchangeRate(new CurrencyPair(ccyFrom, ccyTo), 2.0, ZonedDateTime.now()));

        Assertions.assertEquals(20, p.getAmount());
        Assertions.assertEquals("USD", p.getFiatCcy());
    }

    @ParameterizedTest
    @CsvSource(value = {"true", "false"})
    public void setExchangeRateNull(boolean amountDefined) {
        var p = new Payment(new TestTransaction(new TestLedger(), 10.0, "TEST"));
        p.setExchangeRate(new ExchangeRate(new CurrencyPair("USD", "TEST"), 2.0, ZonedDateTime.now()));
        if (amountDefined) {
            p.setAmount(BigDecimal.valueOf(20), "USD");

        }
        Assertions.assertEquals(20, p.getAmount());
        Assertions.assertEquals("USD", p.getFiatCcy());
        Assertions.assertEquals(10.0, p.getAmountLedgerUnit());

        p.setExchangeRate(null);

        Assertions.assertEquals(amountDefined ? 20 : 0, p.getAmount());
        Assertions.assertEquals("USD", p.getFiatCcy());
        Assertions.assertEquals(amountDefined ? 0 : 10.0, p.getAmountLedgerUnit());
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
            p.setAmount(BigDecimal.valueOf(30), "USD");
            p.setExchangeRate(new ExchangeRate(new CurrencyPair(ccyFrom, ccyTo), 2.0, ZonedDateTime.now()));
        });
    }
}
