package com.radynamics.dallipay.cryptoledger.bitcoin.signing;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Transaction;
import com.radynamics.dallipay.cryptoledger.signing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class BitBox02 implements TransactionSubmitter {
    private final static Logger log = LogManager.getLogger(BitBox02.class);
    private final com.radynamics.dallipay.cryptoledger.bitcoin.Ledger ledger;
    private final TransactionSubmitterInfo info;
    private final ArrayList<TransactionStateListener> stateListener = new ArrayList<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.TransactionSubmitter");

    public final static String Id = "bitbox02";

    public BitBox02(com.radynamics.dallipay.cryptoledger.bitcoin.Ledger ledger) {
        this.ledger = ledger;

        info = new TransactionSubmitterInfo();
        info.setTitle(res.getString("bitbox02.title"));
        info.setDescription(res.getString("bitbox02.desc"));
        info.setDetailUri(URI.create("https://bitbox.swiss"));
        info.setIcon(new FlatSVGIcon("img/bitbox02.svg", 64, 64));
        info.setOrder(100);
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
    public void submit(Transaction[] transactions) {
        var s = ledger.createRpcTransactionSubmitter(getPrivateKeyProvider());
        s.signingMethod(new HwiSigning());
        s.submit(transactions);
    }

    @Override
    public PrivateKeyProvider getPrivateKeyProvider() {
        return new EmptyPrivateKeyProvider();
    }

    @Override
    public TransactionSubmitterInfo getInfo() {
        return info;
    }

    @Override
    public void addStateListener(TransactionStateListener l) {
        stateListener.add(l);
    }

    @Override
    public boolean supportIssuedTokens() {
        return false;
    }

    @Override
    public boolean supportsPathFinding() {
        return false;
    }

    @Override
    public boolean supportsPayload() {
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
