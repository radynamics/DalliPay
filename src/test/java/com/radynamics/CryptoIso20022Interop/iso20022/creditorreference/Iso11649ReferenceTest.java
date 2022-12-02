package com.radynamics.CryptoIso20022Interop.iso20022.creditorreference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Iso11649ReferenceTest {
    @Test
    public void isValid() {
        Assertions.assertFalse(Iso11649Reference.isValid(null));
        Assertions.assertFalse(Iso11649Reference.isValid(""));
        Assertions.assertFalse(Iso11649Reference.isValid("RF"));
        Assertions.assertFalse(Iso11649Reference.isValid("RFa"));
        Assertions.assertFalse(Iso11649Reference.isValid("RF0a"));
        Assertions.assertFalse(Iso11649Reference.isValid("RFa0"));
        Assertions.assertFalse(Iso11649Reference.isValid("RF00"));
        Assertions.assertFalse(Iso11649Reference.isValid("RF180000000000539007547034"));

        Assertions.assertTrue(Iso11649Reference.isValid("RF18 5390 0754 7034"));
        Assertions.assertTrue(Iso11649Reference.isValid("RF18000000000539007547034"));
    }
}
