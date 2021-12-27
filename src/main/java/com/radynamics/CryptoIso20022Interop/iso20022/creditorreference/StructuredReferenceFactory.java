package com.radynamics.CryptoIso20022Interop.iso20022.creditorreference;

import org.apache.commons.lang3.NotImplementedException;

public final class StructuredReferenceFactory {
    public static StructuredReference create(String typeText, String reference) {
        if (typeText == null) throw new IllegalArgumentException("Parameter 'typeText' cannot be null");
        if (reference == null) throw new IllegalArgumentException("Parameter 'reference' cannot be null");
        if (reference.length() == 0) throw new IllegalArgumentException("Parameter 'reference' cannot be empty");

        switch (typeText.toLowerCase()) {
            case "qrr":
                return new StructuredReference() {
                    @Override
                    public ReferenceType getType() {
                        return ReferenceType.SwissQrBill;
                    }

                    @Override
                    public String getUnformatted() {
                        return reference;
                    }
                };
            case "scor":
                return new StructuredReference() {
                    @Override
                    public ReferenceType getType() {
                        return ReferenceType.Scor;
                    }

                    @Override
                    public String getUnformatted() {
                        return reference;
                    }
                };
            case "isr":
                return new StructuredReference() {
                    @Override
                    public ReferenceType getType() {
                        return ReferenceType.Isr;
                    }

                    @Override
                    public String getUnformatted() {
                        return reference;
                    }
                };
            default:
                throw new NotImplementedException(String.format("Structured reference %s unknown.", typeText));
        }
    }
}
