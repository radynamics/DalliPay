package com.radynamics.CryptoIso20022Interop.iso20022.creditorreference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SwissQrReferenceTest {
    @Test
    public void isValid() {
        Assertions.assertFalse(SwissQrReference.isValid(null));
        Assertions.assertFalse(SwissQrReference.isValid(""));
        Assertions.assertFalse(SwissQrReference.isValid("0a"));
        // checksum invalid
        Assertions.assertFalse(SwissQrReference.isValid("978670000000000002200047827"));
        // missing checksum digit
        Assertions.assertFalse(SwissQrReference.isValid("97867000000000000220004782"));
        // non numeric
        Assertions.assertFalse(SwissQrReference.isValid("9786700000000000022000x7826"));
        // too long
        Assertions.assertFalse(SwissQrReference.isValid("9786700000000000022000478267"));

        Assertions.assertTrue(SwissQrReference.isValid("97 86700 00000 00000 22000 47826"));
        Assertions.assertTrue(SwissQrReference.isValid("978670000000000002200047826"));
        Assertions.assertTrue(SwissQrReference.isValid("0"));
        Assertions.assertTrue(SwissQrReference.isValid("000000000000000000000000000"));
    }
}
