package com.radynamics.CryptoIso20022Interop.exchange;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CurrencyPairTest {
    @ParameterizedTest
    @CsvSource({",", "'',''", "EUR,''", "'',EUR"})
    public void ctrNullOrEmpty(String first, String second) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CurrencyPair(first, second);
        });
    }

    @Test
    public void getFirst() {
        Assertions.assertEquals("EUR", new CurrencyPair("EUR", "USD").getFirst());
    }

    @Test
    public void getSecond() {
        Assertions.assertEquals("USD", new CurrencyPair("EUR", "USD").getSecond());
    }

    @Test
    public void contains() {
        var list = new CurrencyPair[]{
                new CurrencyPair("EUR", "USD"),
                new CurrencyPair("CHF", "USD")
        };

        Assertions.assertTrue(CurrencyPair.contains(list, new CurrencyPair("EUR", "USD")));
        Assertions.assertTrue(CurrencyPair.contains(list, new CurrencyPair("CHF", "USD")));

        Assertions.assertFalse(CurrencyPair.contains(list, new CurrencyPair("USD", "EUR")));
        Assertions.assertFalse(CurrencyPair.contains(list, new CurrencyPair("EUR", "CHF")));
    }

    @Test
    public void getDisplayText() {
        Assertions.assertEquals("EUR/USD", new CurrencyPair("EUR", "USD").getDisplayText());
    }

    @Test
    public void affects() {
        Assertions.assertTrue(new CurrencyPair("EUR", "USD").affects("EUR"));
        Assertions.assertTrue(new CurrencyPair("EUR", "USD").affects("USD"));

        Assertions.assertFalse(new CurrencyPair("EUR", "USD").affects("CHF"));
    }

    @Test
    public void invert() {
        var inverted = new CurrencyPair("EUR", "USD").invert();
        Assertions.assertEquals("USD", inverted.getFirst());
        Assertions.assertEquals("EUR", inverted.getSecond());
    }

    @Test
    public void sameAs() {
        var pair = new CurrencyPair("EUR", "USD");
        Assertions.assertTrue(pair.sameAs(new CurrencyPair("EUR", "USD")));

        Assertions.assertFalse(pair.sameAs(null));
        Assertions.assertFalse(pair.sameAs(new CurrencyPair("EUR", "CHF")));
        Assertions.assertFalse(pair.sameAs(new CurrencyPair("USD", "EUR")));
    }

    @Test
    public void isOneToOne() {
        Assertions.assertTrue(new CurrencyPair("EUR", "EUR").isOneToOne());

        Assertions.assertFalse(new CurrencyPair("EUR", "Eur").isOneToOne());
        Assertions.assertFalse(new CurrencyPair("Eur", "EUR").isOneToOne());
        Assertions.assertFalse(new CurrencyPair("EUR", "EUR ").isOneToOne());
        Assertions.assertFalse(new CurrencyPair("EUR", " EUR").isOneToOne());
    }
}
