package com.radynamics.dallipay.cryptoledger.xrpl.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConvertTest {
    @Test
    public void toCurrencyCode() {
        Assertions.assertEquals("USD", Convert.toCurrencyCode("USD"));
        Assertions.assertEquals("EURS", Convert.toCurrencyCode("4555525300000000000000000000000000000000"));
        Assertions.assertEquals("LP B7FD829F075C67B6C87A45FA0E67CF4E5A83A9", Convert.toCurrencyCode("03B7FD829F075C67B6C87A45FA0E67CF4E5A83A9"));
    }

    @Test
    public void fromCurrencyCode() {
        Assertions.assertEquals("USD", Convert.fromCurrencyCode("USD"));
        Assertions.assertEquals("4555525300000000000000000000000000000000", Convert.fromCurrencyCode("EURS"));
    }
}
