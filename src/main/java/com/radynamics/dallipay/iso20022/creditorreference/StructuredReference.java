package com.radynamics.dallipay.iso20022.creditorreference;

public interface StructuredReference {
    ReferenceType getType();

    String getUnformatted();
}
