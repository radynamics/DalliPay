package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;

public class WriterHelper {
    public static StructuredReference[] getStructuredReferences(TransformInstruction transformInstruction, Payment p) {
        var items = p.getStructuredReferences();
        var hasStructuredReferences = items != null && items.length > 0;
        if (!hasStructuredReferences && transformInstruction.getCreditorReferenceIfMissing() != null) {
            items = new StructuredReference[1];
            items[0] = transformInstruction.getCreditorReferenceIfMissing();
        }
        return items;
    }

    public static StringBuilder getUstrd(Payment p) {
        var sb = new StringBuilder();
        for (String s : p.getMessages()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(s);
        }
        return sb;
    }
}
