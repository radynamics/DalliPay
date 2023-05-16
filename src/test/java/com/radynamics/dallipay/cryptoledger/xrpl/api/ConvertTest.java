package com.radynamics.dallipay.cryptoledger.xrpl.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConvertTest {
    @Test
    public void toCurrencyCode() {
        Assertions.assertEquals("USD", Convert.toCurrencyCode("USD"));
        Assertions.assertEquals("EURS", Convert.toCurrencyCode("4555525300000000000000000000000000000000"));
    }

    @Test
    public void fromCurrencyCode() {
        Assertions.assertEquals("USD", Convert.fromCurrencyCode("USD"));
        Assertions.assertEquals("4555525300000000000000000000000000000000", Convert.fromCurrencyCode("EURS"));
    }
}
