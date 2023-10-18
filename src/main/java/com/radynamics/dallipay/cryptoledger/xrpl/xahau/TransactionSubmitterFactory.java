package com.radynamics.dallipay.cryptoledger.xrpl.xahau;

import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.cryptoledger.xrpl.Ledger;
import com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm.XummSigner;

import java.awt.*;

public class TransactionSubmitterFactory implements com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterFactory {
    private final com.radynamics.dallipay.cryptoledger.xrpl.TransactionSubmitterFactory xrplFactory;

    public TransactionSubmitterFactory(Ledger ledger) {
        xrplFactory = new com.radynamics.dallipay.cryptoledger.xrpl.TransactionSubmitterFactory(ledger);
    }

    @Override
    public TransactionSubmitter create(String id, Component parentComponent) {
        if (!XummSigner.Id.equals(id)) {
            throw new IllegalStateException("Unexpected value: " + id);
        }
        return xrplFactory.create(id, parentComponent);
    }

    @Override
    public TransactionSubmitter[] all(Component parentComponent) {
        return new TransactionSubmitter[]{
                create(XummSigner.Id, parentComponent)
        };
    }

    @Override
    public TransactionSubmitter getSuggested(Component parentComponent) {
        return create(XummSigner.Id, parentComponent);
    }
}
