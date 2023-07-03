package com.radynamics.dallipay.iso20022.creditorreference;

import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;

public final class StructuredReferenceFactory {
    public static StructuredReference create(ReferenceType type, String reference) {
        if (reference == null) throw new IllegalArgumentException("Parameter 'reference' cannot be null");
        if (reference.length() == 0) throw new IllegalArgumentException("Parameter 'reference' cannot be empty");

        switch (type) {
            case SwissQrBill:
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
            case Scor:
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
            case Unknown:
                return new StructuredReference() {
                    @Override
                    public ReferenceType getType() {
                        return ReferenceType.Unknown;
                    }

                    @Override
                    public String getUnformatted() {
                        return reference;
                    }
                };
            default:
                throw new NotImplementedException(String.format("Structured reference %s unknown.", type));
        }
    }

    public static ReferenceType getType(String typeText) {
        if (typeText == null) throw new IllegalArgumentException("Parameter 'typeText' cannot be null");
        if (typeText.length() == 0) throw new IllegalArgumentException("Parameter 'typeText' cannot be empty");

        var map = new HashMap<String, ReferenceType>();
        map.put("qrr", ReferenceType.SwissQrBill);
        map.put("scor", ReferenceType.Scor);
        map.put("unk", ReferenceType.Unknown);

        for (var item : map.entrySet()) {
            if (item.getKey().equalsIgnoreCase(typeText)) {
                return item.getValue();
            }
        }

        throw new NotImplementedException(String.format("Structured reference %s unknown.", typeText));
    }

    public static ReferenceType detectType(String ref) {
        if (ref == null) throw new IllegalArgumentException("Parameter 'ref' cannot be null");
        if (ref.length() == 0) throw new IllegalArgumentException("Parameter 'ref' cannot be empty");

        var unformatted = ref.replace(" ", "");
        if (Iso11649Reference.isValid(unformatted)) {
            return ReferenceType.Scor;
        }

        if (SwissQrReference.isValid(unformatted)) {
            return ReferenceType.SwissQrBill;
        }

        return ReferenceType.Unknown;
    }
}
