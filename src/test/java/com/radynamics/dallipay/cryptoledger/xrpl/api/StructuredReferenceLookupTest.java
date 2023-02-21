package com.radynamics.dallipay.cryptoledger.xrpl.api;

import com.radynamics.dallipay.iso20022.Utils;
import com.radynamics.dallipay.iso20022.creditorreference.ReferenceType;
import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.*;

import java.io.UnsupportedEncodingException;

public class StructuredReferenceLookupTest {
    @Test
    public void ctr() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new StructuredReferenceLookup(null);
        });
    }

    @Test
    public void find() throws DecoderException, UnsupportedEncodingException {
        {
            var p = preparePayment(new String[0])
                    .build();

            var found = new StructuredReferenceLookup(p).find();
            Assertions.assertNotNull(found);
            Assertions.assertEquals(0, found.length);
        }
        {
            var p = preparePayment(new String[]{"RF18000000000539007547034"})
                    .build();

            var found = new StructuredReferenceLookup(p).find();
            Assertions.assertEquals(1, found.length);
            Assertions.assertEquals(ReferenceType.Scor, found[0].getType());
        }
        {
            var p = preparePayment(new String[]{"RF18 5390 0754 7034"})
                    .build();

            var found = new StructuredReferenceLookup(p).find();
            Assertions.assertEquals(1, found.length);
            Assertions.assertEquals(ReferenceType.Scor, found[0].getType());
        }
        {
            var p = preparePayment(new String[]{"Invoice RF18000000000539007547034"})
                    .build();

            var found = new StructuredReferenceLookup(p).find();
            Assertions.assertEquals(1, found.length);
            Assertions.assertEquals(ReferenceType.Scor, found[0].getType());
        }
        {
            var p = preparePayment(new String[]{"a  b"})
                    .build();

            var found = new StructuredReferenceLookup(p).find();
            Assertions.assertEquals(0, found.length);
        }
        {
            var p = preparePayment(new String[]{"any free text", "Invoice RF18000000000539007547034"})
                    .build();

            var found = new StructuredReferenceLookup(p).find();
            Assertions.assertEquals(1, found.length);
            Assertions.assertEquals(ReferenceType.Scor, found[0].getType());
        }
    }

    private ImmutablePayment.Builder preparePayment(String[] memoText) {
        var mb = Memo.builder();
        for (var text : memoText) {
            mb.memoData(Utils.stringToHex(text));
        }
        var mw = MemoWrapper.builder()
                .memo(mb.build())
                .build();

        return Payment.builder()
                .account(Address.of("rGVKSdv3sWYbk6eWqqbUzFZwq252jvDgmn"))
                .destination(Address.of("rsVWYm2azv93KYwVUKQCexx22MpaCxUsZX"))
                .fee(XrpCurrencyAmount.ofDrops(10))
                .amount(XrpCurrencyAmount.ofDrops(100))
                .addMemos(mw);
    }
}
