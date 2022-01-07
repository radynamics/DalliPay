package com.radynamics.CryptoIso20022Interop.iso20022;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.Assert.assertEquals;

public class IbanAccountTest {
    @ParameterizedTest
    @CsvSource(value = {"null", "''"}, nullValues = {"null"})
    public void ctrNullOrEmpty(String unformatted) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new IbanAccount(unformatted);
        });
    }

    @Test
    public void getUnformatted() {
        assertEquals("CH9300762011623852957", new IbanAccount("CH9300762011623852957").getUnformatted());
        assertEquals("CH9300762011623852957", new IbanAccount("CH93 0076 2011 6238 5295 7").getUnformatted());
        assertEquals("FI2112345600000785", new IbanAccount("FI2112345600000785").getUnformatted());
        assertEquals("BR1800360305000010009795493C1", new IbanAccount("BR1800360305000010009795493C1").getUnformatted());
    }
}
