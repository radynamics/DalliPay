package com.radynamics.dallipay.cryptoledger.generic;

import com.radynamics.dallipay.cryptoledger.memo.PayloadConverter;
import com.radynamics.dallipay.iso20022.creditorreference.ReferenceType;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReference;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReferenceFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;

public final class StructuredReferenceLookup {
    public static StructuredReference[] find(String memoText) {
        var list = new ArrayList<StructuredReference>();

        var unwrappedMemo = PayloadConverter.fromMemo(memoText);
        for (var r : unwrappedMemo.structuredReferences()) {
            list.add(r);
        }

        for (var ft : unwrappedMemo.freeTexts()) {
            var fromMemoText = fromMemoText(ft);
            if (fromMemoText != null) {
                list.add(fromMemoText);
            }
        }

        return list.toArray(new StructuredReference[0]);
    }

    private static StructuredReference fromMemoText(String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }

        var refType = new HashSet<ReferenceType>();
        refType.add(ReferenceType.Scor);
        refType.add(ReferenceType.SwissQrBill);

        // Single word "RF18000000000539007547034"
        {
            var type = StructuredReferenceFactory.detectType(text);
            if (refType.contains(type)) {
                return StructuredReferenceFactory.create(type, text);
            }
        }

        // Formatted as multiple words "RF18 5390 0754 7034" (sender copy&pasted formatted text)
        {
            var candidate = StringUtils.deleteWhitespace(text);
            var type = StructuredReferenceFactory.detectType(candidate);
            if (refType.contains(type)) {
                return StructuredReferenceFactory.create(type, candidate);
            }
        }

        // Multiple words "Invoice RF18000000000539007547034"
        {
            for (var word : text.split(" ")) {
                var candidate = StringUtils.deleteWhitespace(word);
                if (candidate.length() == 0) {
                    continue;
                }
                var type = StructuredReferenceFactory.detectType(candidate);
                if (refType.contains(type)) {
                    return StructuredReferenceFactory.create(type, candidate);
                }
            }
        }

        return null;
    }
}
