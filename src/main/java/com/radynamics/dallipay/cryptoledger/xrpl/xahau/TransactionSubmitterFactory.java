package com.radynamics.dallipay.cryptoledger.xrpl.xahau;

import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.cryptoledger.xrpl.Ledger;
import com.radynamics.dallipay.cryptoledger.xrpl.signing.Crossmark;
import com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm.XummSigner;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TransactionSubmitterFactory implements com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterFactory {
    private final com.radynamics.dallipay.cryptoledger.xrpl.TransactionSubmitterFactory xrplFactory;

    private final Set<String> supported = new HashSet<>(Set.of(XummSigner.Id, Crossmark.Id));

    public TransactionSubmitterFactory(Ledger ledger) {
        xrplFactory = new com.radynamics.dallipay.cryptoledger.xrpl.TransactionSubmitterFactory(ledger);
    }

    @Override
    public TransactionSubmitter create(String id, Component parentComponent) {
        if (!supported.contains(id)) {
            throw new IllegalStateException("Unexpected value: " + id);
        }
        return xrplFactory.create(id, parentComponent);
    }

    @Override
    public TransactionSubmitter[] all(Component parentComponent) {
        var list = new ArrayList<TransactionSubmitter>();
        for (var s : supported) {
            list.add(create(s, parentComponent));
        }
        return list.toArray(new TransactionSubmitter[0]);
    }

    @Override
    public TransactionSubmitter getSuggested(Component parentComponent) {
        return create(XummSigner.Id, parentComponent);
    }
}
