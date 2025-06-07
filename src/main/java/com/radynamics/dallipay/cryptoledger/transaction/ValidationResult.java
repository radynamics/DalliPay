package com.radynamics.dallipay.cryptoledger.transaction;

public class ValidationResult {
    private ValidationState status;
    private String message;

    public ValidationResult(ValidationState status, String message) {
        if (message == null) throw new IllegalArgumentException("Parameter 'message' cannot be null");
        if (message.length() == 0) throw new IllegalArgumentException("Parameter 'message' cannot be empty");
        this.status = status;
        this.message = message;
    }

    public static ValidationResult of(Throwable t) {
        return new ValidationResult(ValidationState.Error, t.getMessage());
    }

    public ValidationState getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
