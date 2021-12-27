package com.radynamics.CryptoIso20022Interop.iso20022.creditorreference;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public class StructuredReferenceFactoryTest {
    @ParameterizedTest
    @CsvSource({",", ",\"\""})
    public void createTypeTextNull(String typeText, String reference) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            StructuredReferenceFactory.create(typeText, reference);
        });
    }

    @ParameterizedTest
    @CsvSource({"\"\",\"\"", "\"unknown\",\"\""})
    public void createTypeTextUnknown(String typeText, String reference) {
        Assertions.assertThrows(NotImplementedException.class, () -> {
            StructuredReferenceFactory.create(typeText, reference);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"qrr", "scor", "isr"})
    public void createReference(String typeText) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            StructuredReferenceFactory.create(typeText, null);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            StructuredReferenceFactory.create(typeText, "");
        });
    }
}
