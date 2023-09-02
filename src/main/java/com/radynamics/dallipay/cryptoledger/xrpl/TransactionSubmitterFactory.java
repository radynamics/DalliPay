package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.Secrets;
import com.radynamics.dallipay.cryptoledger.OnchainVerifier;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.cryptoledger.xrpl.signing.Crossmark;
import com.radynamics.dallipay.cryptoledger.xrpl.signing.GemWallet;
import com.radynamics.dallipay.cryptoledger.xrpl.signing.RpcSubmitter;
import com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm.DatabaseStorage;
import com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm.XummSigner;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class TransactionSubmitterFactory implements com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterFactory {
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
                var apiKey = Secrets.getXummApiKey();
                if (apiKey == null) {
                    throw new RuntimeException("No apiKey for Xumm available.");
                }
                var signer = new XummSigner(ledger, apiKey);
                signer.setStorage(new DatabaseStorage());
                signer.setVerifier(new OnchainVerifier(ledger));
                return signer;
            }
            case GemWallet.Id: {
                var signer = new GemWallet(ledger);
                signer.setVerifier(new OnchainVerifier(ledger));
                return signer;
            }
            case Crossmark.Id: {
                var signer = new Crossmark(ledger);
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
        if (Secrets.getXummApiKey() != null) {
            list.add(create(XummSigner.Id, parentComponent));
        }
        list.add(create(GemWallet.Id, parentComponent));
        list.add(create(Crossmark.Id, parentComponent));

        return list.toArray(new TransactionSubmitter[0]);
    }

    @Override
    public TransactionSubmitter getSuggested(Component parentComponent) {
        var all = Arrays.asList(all(parentComponent));
        all.sort(Comparator.comparingInt(o -> o.getInfo().getOrder()));
        return all.size() == 0 ? null : all.get(all.size() - 1);
    }
}
