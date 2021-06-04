package com.radynamics.CryptoIso20022Interop.iso20022.creditorreference;

public interface StructuredReference {
    ReferenceType getType();

    String getUnformatted();
}
