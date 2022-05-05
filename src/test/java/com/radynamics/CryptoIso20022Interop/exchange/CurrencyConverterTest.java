package com.radynamics.CryptoIso20022Interop.exchange;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

public class CurrencyConverterTest {
    private final CurrencyConverter ccyConverter;

    public CurrencyConverterTest() {
        ExchangeRate[] rates = {
                new ExchangeRate("CHF", "TEST", 0.9, ZonedDateTime.now()),
                new ExchangeRate("EUR", "TEST", 1.1, ZonedDateTime.now()),
                new ExchangeRate("JPY", "TEST", 0.0088, ZonedDateTime.now()), // sometimes defined with factor 100
                new ExchangeRate("XXX", "TEST", 0.888888, ZonedDateTime.now()),
                new ExchangeRate("XXX1", "TEST", 0.88888888, ZonedDateTime.now()),
        };
        ccyConverter = new CurrencyConverter(rates);
    }

    @ParameterizedTest
    @CsvSource({",", "'',''"})
    public void convertCcyNullOrEmpty(String sourceCcy, String targetCcy) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ccyConverter.convert(BigDecimal.valueOf(100), sourceCcy, targetCcy);
        });
    }

    @ParameterizedTest
    @CsvSource({"\"ABC\",\"CHF\"", "\"CHF\",\"ABC\"", "\"ABC\",\"DEF\"", "\"CHF\",\"EUR\""})
    public void convertCcyUnknown(String sourceCcy, String targetCcy) {
        Assertions.assertThrows(RuntimeException.class, () -> {
            ccyConverter.convert(BigDecimal.valueOf(100), sourceCcy, targetCcy);
        });
    }

    @Test
    public void convert() {
        assertEquals(90, ccyConverter.convert(BigDecimal.valueOf(90), "CHF", "CHF"), 0);

        assertEquals(90, ccyConverter.convert(BigDecimal.valueOf(100), "CHF", "TEST"), 0);
        assertEquals(110, ccyConverter.convert(BigDecimal.valueOf(100), "EUR", "TEST"), 0);
        assertEquals(0.88, ccyConverter.convert(BigDecimal.valueOf(100), "JPY", "TEST"), 0);
        assertEquals(88.8888, ccyConverter.convert(BigDecimal.valueOf(100), "XXX", "TEST"), 0);
        assertEquals(88.88889, ccyConverter.convert(BigDecimal.valueOf(100), "XXX1", "TEST"), 0);

        assertEquals(111.11111, ccyConverter.convert(BigDecimal.valueOf(100), "TEST", "CHF"), 0);
        assertEquals(90.909090, ccyConverter.convert(BigDecimal.valueOf(100), "TEST", "EUR"), 0);
        assertEquals(11363.63636, ccyConverter.convert(BigDecimal.valueOf(100), "TEST", "JPY"), 0);
        assertEquals(112.50011, ccyConverter.convert(BigDecimal.valueOf(100), "TEST", "XXX"), 0);
        assertEquals(112.50000, ccyConverter.convert(BigDecimal.valueOf(100), "TEST", "XXX1"), 0);
    }

    @Test
    public void has() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ccyConverter.has(null);
        });

        assertTrue(ccyConverter.has(new CurrencyPair("CHF", "TEST")));
        assertTrue(ccyConverter.has(new CurrencyPair("TEST", "CHF")));

        assertFalse(ccyConverter.has(new CurrencyPair("CHF", "TEST1")));
    }

    @Test
    public void get() {
        ExchangeRate[] rates = {
                new ExchangeRate("TEST", "TEST", 1.0, ZonedDateTime.now()),
                new ExchangeRate("CHF", "TEST", 0.9, ZonedDateTime.now()),
                new ExchangeRate("EUR", "TEST", 1.1, ZonedDateTime.now()),
        };
        var ccyConverter = new CurrencyConverter(rates);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ccyConverter.get(null);
        });

        for (var ccy : new String[]{"TEST", "CHF", "EUR"}) {
            var r = ccyConverter.get(new CurrencyPair(ccy, "TEST"));
            Assertions.assertNotNull(r);
            Assertions.assertEquals(ccy, r.getPair().getFirst());
            Assertions.assertEquals("TEST", r.getPair().getSecond());
        }

        Assertions.assertNull(ccyConverter.get(new CurrencyPair("USD", "TEST")));
    }
}
