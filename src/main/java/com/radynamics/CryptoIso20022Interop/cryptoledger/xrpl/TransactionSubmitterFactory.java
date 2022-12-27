package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.Secrets;
import com.radynamics.CryptoIso20022Interop.cryptoledger.OnchainVerifier;
import com.radynamics.CryptoIso20022Interop.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.RpcSubmitter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.xumm.DatabaseStorage;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.xumm.XummSigner;

import java.awt.*;
import java.io.IOException;

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
}
