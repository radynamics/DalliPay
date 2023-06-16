package com.radynamics.dallipay.cryptoledger.xrpl.api;

import com.radynamics.dallipay.iso20022.Utils;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReference;
import org.apache.commons.codec.DecoderException;
import org.xrpl.xrpl4j.model.transactions.MemoWrapper;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class StructuredReferenceLookup {
    private final Transaction trx;

    public StructuredReferenceLookup(org.xrpl.xrpl4j.model.transactions.Transaction trx) {
        if (trx == null) throw new IllegalArgumentException("Parameter 'trx' cannot be null");
        this.trx = trx;
    }

    public StructuredReference[] find() throws DecoderException, UnsupportedEncodingException {
        var list = new ArrayList<StructuredReference>();

        for (MemoWrapper mw : trx.memos()) {
            if (mw.memo().memoData().isEmpty()) {
                continue;
            }

            var memoText = Utils.hexToString(mw.memo().memoData().get());
            list.addAll(List.of(com.radynamics.dallipay.cryptoledger.generic.StructuredReferenceLookup.find(memoText)));
        }

        return list.toArray(new StructuredReference[0]);
    }
}
