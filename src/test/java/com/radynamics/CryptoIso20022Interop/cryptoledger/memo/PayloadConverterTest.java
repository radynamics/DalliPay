package com.radynamics.CryptoIso20022Interop.cryptoledger.memo;

import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.ReferenceType;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReferenceFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class PayloadConverterTest {
    @Test
    public void toMemoParam() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            PayloadConverter.toMemo(null, null);
        });
    }

    @Test
    public void toMemo() {
        assertEquals("{\"CdOrPrtry\":[],\"v\":1,\"ft\":[]}",
                PayloadConverter.toMemo(new StructuredReference[0], new String[0]));

        var refs = new ArrayList<StructuredReference>();
        refs.add(StructuredReferenceFactory.create(ReferenceType.SwissQrBill, "210000000003139471430009017"));

        assertEquals("{\"CdOrPrtry\":[{\"t\":\"qrr\",\"v\":\"210000000003139471430009017\"}],\"v\":1,\"ft\":[]}",
                PayloadConverter.toMemo(refs.toArray(new StructuredReference[0]), new String[0]));

        var freeTexts = new ArrayList<String>();
        freeTexts.add("test2");
        freeTexts.add("test1");
        assertEquals("{\"CdOrPrtry\":[{\"t\":\"qrr\",\"v\":\"210000000003139471430009017\"}],\"v\":1,\"ft\":[\"test2\",\"test1\"]}",
                PayloadConverter.toMemo(refs.toArray(new StructuredReference[0]), freeTexts.toArray(new String[0])));

        refs.add(StructuredReferenceFactory.create(ReferenceType.Scor, "RF18539007547034"));
        assertEquals("{\"CdOrPrtry\":[{\"t\":\"qrr\",\"v\":\"210000000003139471430009017\"},{\"t\":\"scor\",\"v\":\"RF18539007547034\"}],\"v\":1,\"ft\":[]}",
                PayloadConverter.toMemo(refs.toArray(new StructuredReference[0]), new String[0]));
    }

    @Test
    public void fromMemoNull() {
        assertNull(PayloadConverter.fromMemo(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "test1",
            "{]",
            "{\"CdOrPrtry\":\"invalid\",\"v\":1}",
            "{\"CdOrPrtry\":[\"invalid\"],\"v\":1}",
            "{\"CdOrPrtry\":{\"invalid\":2},\"v\":1}",
            "{\"CdOrPrtry\":[{\"t_\":\"qrr\",\"v\":\"210000000003139471430009017\"}],\"v\":1,\"ft\":[]}",
            "{\"CdOrPrtry\":[{\"t\":\"qrr_\",\"v\":\"210000000003139471430009017\"}],\"v\":1,\"ft\":[]}",
    })
    public void fromMemoFreeTexts(String json) {
        var md = PayloadConverter.fromMemo(json);
        assertNotNull(md);
        assertEquals(0, md.structuredReferences().length);
        assertEquals(1, md.freeTexts().length);
        assertEquals(json, md.freeTexts()[0]);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"CdOrPrtry\":[{\"t\":\"scor\",\"v\":\"RF712348231\"}],\"v\":1}",
            "{\"CdOrPrtry\":[{\"t\":\"scor\",\"v\":\"RF712348231\"}],\"v\":1,\"ft\":[]}",
    })
    public void fromMemoWithoutFreetexts(String json) {
        var md = PayloadConverter.fromMemo(json);
        assertNotNull(md);
        assertEquals(1, md.structuredReferences().length);
        assertEquals(ReferenceType.Scor, md.structuredReferences()[0].getType());
        assertEquals("RF712348231", md.structuredReferences()[0].getUnformatted());
        assertEquals(0, md.freeTexts().length);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"v\":1,\"ft\":[\"test1\"]}}",
            "{\"CdOrPrtry_\":[{\"t\":\"scor\",\"v\":\"RF712348231\"}],\"v\":1,\"ft\":[\"test1\"]}",
            "{\"CdOrPrtry\":[{\"t\":\"scor\"}],\"v\":1,\"ft\":[\"test1\"]}",
            "{\"CdOrPrtry\":[{\"t\":\"scor\",\"v\":\"\"}],\"v\":1,\"ft\":[\"test1\"]}",
            "{\"CdOrPrtry\":[{\"t\":\"scor\",\"v\":null}],\"v\":1,\"ft\":[\"test1\"]}",
            "{\"CdOrPrtry\":[{\"t\":\"scor\",\"v_\":\"RF712348231\"}],\"v\":1,\"ft\":[\"test1\"]}",
    })
    public void fromMemoWithoutReference(String json) {
        var md = PayloadConverter.fromMemo(json);
        assertNotNull(md);
        assertEquals(0, md.structuredReferences().length);
        assertEquals(1, md.freeTexts().length);
        assertEquals("test1", md.freeTexts()[0]);
    }

    @Test
    public void fromMemoWithoutReferenceAndFreetexts() {
        var md = PayloadConverter.fromMemo("{\"v\":1}}");
        assertNotNull(md);
        assertEquals(0, md.structuredReferences().length);
        assertEquals(0, md.freeTexts().length);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 99})
    public void fromMemoInvalidVersion(int version) {
        var json = String.format("{\"CdOrPrtry\":[{\"t\":\"qrr\",\"v\":\"210000000003139471430009017\"}],\"v\":%s,\"ft\":[\"test1\"]}", version);
        assertNull(PayloadConverter.fromMemo(json));
    }

    @Test
    public void fromMemoQrr() {
        var md = PayloadConverter.fromMemo("{\"CdOrPrtry\":[{\"t\":\"qrr\",\"v\":\"210000000003139471430009017\"}],\"v\":1,\"ft\":[\"test2\",\"test1\"]}");
        assertNotNull(md);
        assertEquals(1, md.structuredReferences().length);
        assertEquals(ReferenceType.SwissQrBill, md.structuredReferences()[0].getType());
        assertEquals("210000000003139471430009017", md.structuredReferences()[0].getUnformatted());
        assertEquals(2, md.freeTexts().length);
        assertEquals("test2", md.freeTexts()[0]);
        assertEquals("test1", md.freeTexts()[1]);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void fromMemoScor(boolean formatted) {
        var scor = formatted ? "RF71 2348 231" : "RF712348231";
        var md = PayloadConverter.fromMemo(String.format("{\"CdOrPrtry\":[{\"t\":\"scor\",\"v\":\"%s\"}],\"v\":1,\"ft\":[]}", scor));
        assertNotNull(md);
        assertEquals(1, md.structuredReferences().length);
        assertEquals(ReferenceType.Scor, md.structuredReferences()[0].getType());
        assertEquals("RF712348231", md.structuredReferences()[0].getUnformatted());
        assertEquals(0, md.freeTexts().length);
    }
}
