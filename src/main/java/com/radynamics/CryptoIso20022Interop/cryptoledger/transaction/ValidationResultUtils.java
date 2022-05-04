package com.radynamics.CryptoIso20022Interop.cryptoledger.transaction;

import java.util.Arrays;

public class ValidationResultUtils {
    public static ValidationResult[] fromError(ValidationResult[] validations) {
        return Arrays.stream(validations).filter(o -> o.getStatus() == ValidationState.Error).toArray(ValidationResult[]::new);
    }

    public static ValidationResult[] fromWarning(ValidationResult[] validations) {
        return Arrays.stream(validations).filter(o -> o.getStatus() == ValidationState.Error || o.getStatus() == ValidationState.Warning).toArray(ValidationResult[]::new);
    }

    public static void sortDescending(ValidationResult[] validations) {
        Arrays.sort(validations, (a, b) -> Integer.compare(b.getStatus().getLevel(), a.getStatus().getLevel()));
    }
}
