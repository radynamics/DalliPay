package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.Secrets;
import com.radynamics.CryptoIso20022Interop.cryptoledger.OnchainVerifier;
import com.radynamics.CryptoIso20022Interop.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.RpcSubmitter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.xumm.DatabaseStorage;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.xumm.XummSigner;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class TransactionSubmitterFactory implements com.radynamics.CryptoIso20022Interop.cryptoledger.signing.TransactionSubmitterFactory {
    private final Ledger ledger;

    public TransactionSubmitterFactory(Ledger ledger) {
        this.ledger = ledger;
    }

    @Override
    public TransactionSubmitter create(String id, Component parentComponent) {
        switch (id) {
            case RpcSubmitter.Id: {
                return ledger.createRpcTransactionSubmitter(parentComponent);
            }
            case XummSigner.Id: {
                var secrets = new Secrets();
                try {
                    secrets.read();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                var signer = new XummSigner(secrets.getXummApiKey());
                signer.setStorage(new DatabaseStorage());
                signer.setVerifier(new OnchainVerifier(ledger));
                return signer;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + id);
        }
    }

    @Override
    public TransactionSubmitter[] all(Component parentComponent) {
        var list = new ArrayList<TransactionSubmitter>();

        list.add(create(RpcSubmitter.Id, parentComponent));
        list.add(create(XummSigner.Id, parentComponent));

        return list.toArray(new TransactionSubmitter[0]);
    }

    @Override
    public TransactionSubmitter getSuggested(Component parentComponent) {
        var all = all(parentComponent);
        for (var s : all) {
            if (s.getInfo().isRecommended()) {
                return s;
            }
        }
        return all.length == 0 ? null : all[0];
    }
}
