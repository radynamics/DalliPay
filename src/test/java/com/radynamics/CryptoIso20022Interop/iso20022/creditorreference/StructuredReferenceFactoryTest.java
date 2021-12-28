package com.radynamics.CryptoIso20022Interop.iso20022.creditorreference;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public class StructuredReferenceFactoryTest {
    @ParameterizedTest
    @CsvSource(value = {"null", "''"}, nullValues = {"null"})
    public void getTypeNullOrEmpty(String typeText) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            StructuredReferenceFactory.getType(typeText);
        });
    }

    @ParameterizedTest
    @CsvSource({"unknown", "abc"})
    public void createTypeTextUnknown(String typeText) {
        Assertions.assertThrows(NotImplementedException.class, () -> {
            StructuredReferenceFactory.getType(typeText);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"qrr", "scor", "isr"})
    public void createReference(String typeText) {
        var type = StructuredReferenceFactory.getType(typeText);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            StructuredReferenceFactory.create(type, null);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            StructuredReferenceFactory.create(type, "");
        });
    }
}
