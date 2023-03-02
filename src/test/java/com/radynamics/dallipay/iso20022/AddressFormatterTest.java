package com.radynamics.dallipay.iso20022;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AddressFormatterTest {
    @Test
    public void formatSingleLine() {
        Assertions.assertEquals("Test", AddressFormatter.formatSingleLine(new Address("Test")));
        Assertions.assertEquals("Test AG", AddressFormatter.formatSingleLine(new Address("Test AG")));
        {
            var a = new Address("Test AG");
            a.setCity("Zurich");
            Assertions.assertEquals("Test AG, Zurich", AddressFormatter.formatSingleLine(a));
        }
        {
            var a = new Address("Test AG");
            a.setZip("8000");
            Assertions.assertEquals("Test AG", AddressFormatter.formatSingleLine(a));
        }
        {
            var a = new Address("Test AG");
            a.setZip("8000");
            a.setCity("Zurich");
            Assertions.assertEquals("Test AG, 8000 Zurich", AddressFormatter.formatSingleLine(a));
        }
        {
            var a = new Address("Test AG");
            a.setZip("8000");
            a.setCity("Zurich");
            a.setCountryShort("CH");
            Assertions.assertEquals("Test AG, 8000 Zurich (CH)", AddressFormatter.formatSingleLine(a));
        }
        {
            var a = new Address("Test AG");
            a.setStreet("Mainstreet 5");
            a.setZip("8000");
            a.setCity("Zurich");
            a.setCountryShort("CH");
            Assertions.assertEquals("Test AG, 8000 Zurich (CH)", AddressFormatter.formatSingleLine(a));
        }
    }
}
