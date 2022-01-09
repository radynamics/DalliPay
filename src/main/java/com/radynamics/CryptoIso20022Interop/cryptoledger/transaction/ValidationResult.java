package com.radynamics.CryptoIso20022Interop.cryptoledger.transaction;

public class ValidationResult {
    private ValidationState status;
    private String message;

    public ValidationResult(ValidationState status, String message) {
        if (message == null) throw new IllegalArgumentException("Parameter 'message' cannot be null");
        if (message.length() == 0) throw new IllegalArgumentException("Parameter 'message' cannot be empty");
        this.status = status;
        this.message = message;
    }

    public ValidationState getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
