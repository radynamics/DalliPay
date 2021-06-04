package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.ReferenceType;
import org.apache.commons.lang3.NotImplementedException;

public class CreditorReferenceConverter {
    public static String toPrtry(ReferenceType value) {
        switch (value) {
            case Isr:
                return "ISR Reference";
            case Scor:
                return "SCOR";
            case SwissQrBill:
                return "QRR";
            default:
                throw new NotImplementedException(value.name());
        }
    }
}
