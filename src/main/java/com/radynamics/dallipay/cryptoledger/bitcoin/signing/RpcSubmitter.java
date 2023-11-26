package com.radynamics.dallipay.cryptoledger.bitcoin.signing;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.OnchainVerifier;
import com.radynamics.dallipay.cryptoledger.Transaction;
import com.radynamics.dallipay.cryptoledger.memo.PayloadConverter;
import com.radynamics.dallipay.cryptoledger.signing.PrivateKeyProvider;
import com.radynamics.dallipay.cryptoledger.signing.TransactionStateListener;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class RpcSubmitter implements TransactionSubmitter {
    private final static Logger log = LogManager.getLogger(RpcSubmitter.class);
    private final Ledger ledger;
    private final PrivateKeyProvider privateKeyProvider;
    private OnchainVerifier verifier;
    private final TransactionSubmitterInfo info;
    private final ArrayList<TransactionStateListener> stateListener = new ArrayList<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.TransactionSubmitter");

    public final static String Id = "rpcSubmitter";

    public RpcSubmitter(Ledger ledger, PrivateKeyProvider privateKeyProvider) {
        this.ledger = ledger;
        this.privateKeyProvider = privateKeyProvider;

        info = new TransactionSubmitterInfo();
        info.setTitle(res.getString("rpc.title"));
        info.setDescription(res.getString("rpc.desc"));
        info.setNotRecommended(true);
    }

    @Override
    public String getId() {
        return Id;
    }

    @Override
    public Ledger getLedger() {
        return ledger;
    }

    @Override
    public void submit(com.radynamics.dallipay.cryptoledger.Transaction[] transactions) {
        var client = new BitcoinJSONRPCClient(ledger.getNetwork().getUrl().url());
        for (var t : transactions) {
            // Necessary if wallet is encrypted.
            client.walletPassPhrase(privateKeyProvider.get(t.getSenderWallet().getPublicKey()), Duration.ofSeconds(5).toMillis());

            var amount = BigDecimal.valueOf(t.getAmount().getNumber().doubleValue());
            // TODO: include comment data. This api call does NOT include comment into the transaction itself.
            var comment = PayloadConverter.toMemo(t.getStructuredReferences(), t.getMessages());
            client.sendToAddress(t.getReceiverWallet().getPublicKey(), amount, comment, comment);
        }
    }

    @Override
    public PrivateKeyProvider getPrivateKeyProvider() {
        return privateKeyProvider;
    }

    @Override
    public TransactionSubmitterInfo getInfo() {
        return info;
    }

    public void setVerifier(OnchainVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public void addStateListener(TransactionStateListener l) {
        stateListener.add(l);
    }

    @Override
    public boolean supportIssuedTokens() {
        return true;
    }

    @Override
    public boolean supportsPathFinding() {
        return false;
    }

    @Override
    public void deleteSettings() {
    }

    private void raiseSuccess(Transaction t) {
        for (var l : stateListener) {
            l.onSuccess(t);
        }
    }

    private void raiseFailure(Transaction t) {
        for (var l : stateListener) {
            l.onFailure(t);
        }
    }
}
