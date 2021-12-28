package com.radynamics.CryptoIso20022Interop.cryptoledger.memo;

import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.ReferenceType;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReferenceFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class StructuredReferenceConverterTest {
    @Test
    public void toMemoReferenceNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            StructuredReferenceConverter.toMemo(null);
        });
    }

    @Test
    public void toMemoQrr() {
        assertEquals("{\"t\":\"qrr\",\"v\":\"210000000003139471430009017\"}",
                StructuredReferenceConverter.toMemo(StructuredReferenceFactory.create(ReferenceType.SwissQrBill, "210000000003139471430009017")).toString());
    }

    @Test
    public void toMemoScor() {
        assertEquals("{\"t\":\"scor\",\"v\":\"RF18539007547034\"}",
                StructuredReferenceConverter.toMemo(StructuredReferenceFactory.create(ReferenceType.Scor, "RF18539007547034")).toString());
        assertEquals("{\"t\":\"scor\",\"v\":\"RF18000000000539007547034\"}",
                StructuredReferenceConverter.toMemo(StructuredReferenceFactory.create(ReferenceType.Scor, "RF18000000000539007547034")).toString());
    }

    @Test
    public void toMemoIsr() {
        assertEquals("{\"t\":\"isr\",\"v\":\"814991000000006407610246697\"}",
                StructuredReferenceConverter.toMemo(StructuredReferenceFactory.create(ReferenceType.Isr, "814991000000006407610246697")).toString());
    }
}
