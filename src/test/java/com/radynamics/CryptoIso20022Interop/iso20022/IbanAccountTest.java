package com.radynamics.CryptoIso20022Interop.iso20022;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.Assert.*;

public class IbanAccountTest {
    @ParameterizedTest
    @CsvSource(value = {"null", "''"}, nullValues = {"null"})
    public void ctrNullOrEmpty(String unformatted) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new IbanAccount(unformatted);
        });
    }

    @Test
    public void empty() {
        assertEquals(IbanAccount.Empty, IbanAccount.Empty);
    }

    @Test
    public void getUnformatted() {
        assertEquals("", IbanAccount.Empty.getUnformatted());
        assertEquals("CH9300762011623852957", new IbanAccount("CH9300762011623852957").getUnformatted());
        assertEquals("CH9300762011623852957", new IbanAccount("CH93 0076 2011 6238 5295 7").getUnformatted());
        assertEquals("FI2112345600000785", new IbanAccount("FI2112345600000785").getUnformatted());
        assertEquals("BR1800360305000010009795493C1", new IbanAccount("BR1800360305000010009795493C1").getUnformatted());
    }

    @Test
    public void getFormatted() {
        assertEquals("", IbanAccount.Empty.getFormatted());
        assertEquals("CH93 0076 2011 6238 5295 7", new IbanAccount("CH9300762011623852957").getFormatted());
        assertEquals("CH93 0076 2011 6238 5295 7", new IbanAccount("CH93 0076 2011 6238 5295 7").getFormatted());
        assertEquals("FI21 1234 5600 0007 85", new IbanAccount("FI2112345600000785").getFormatted());
        assertEquals("BR18 0036 0305 0000 1000 9795 493C 1", new IbanAccount("BR1800360305000010009795493C1").getFormatted());
    }

    @Test
    public void isValid() {
        assertTrue(IbanAccount.isValid("CH9300762011623852957"));
        assertTrue(IbanAccount.isValid(" CH9300762011623852957"));
        assertTrue(IbanAccount.isValid("CH9300762011623852957 "));
        assertTrue(IbanAccount.isValid("CH 9300762011623852957"));
        assertTrue(IbanAccount.isValid("CH93 0076 2011 6238 5295 7"));

        assertFalse(IbanAccount.isValid(IbanAccount.Empty.getUnformatted()));
        assertFalse(IbanAccount.isValid(null));
        assertFalse(IbanAccount.isValid(""));
        assertFalse(IbanAccount.isValid(" "));
        assertFalse(IbanAccount.isValid("a"));
        assertFalse(IbanAccount.isValid("CH93"));
        assertFalse(IbanAccount.isValid("CH93 0076"));
        assertFalse(IbanAccount.isValid("CH9300762011623852956")); // checksum invalid
        assertFalse(IbanAccount.isValid("CH930076x011623852956")); // char invalid
    }
}
