package com.radynamics.CryptoIso20022Interop.cryptoledger.memo;

import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.ReferenceType;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;
import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONObject;

public final class StructuredReferenceConverter {
    public static JSONObject toMemo(StructuredReference ref) {
        if (ref == null) {
            throw new IllegalArgumentException("Parameter 'ref' cannot be null");
        }

        var json = new JSONObject();

        json.put("t", toType(ref.getType())); // subtype, ex. QRR, SCOR
        json.put("v", ref.getUnformatted());

        return json;
    }

    private static String toType(ReferenceType t) {
        switch (t) {
            case Isr -> {
                return "isr";
            }
            case Scor -> {
                return "scor";
            }
            case SwissQrBill -> {
                return "qrr";
            }
            default -> throw new NotImplementedException(String.format("Structured reference %s unknown.", t));
        }
    }
}
