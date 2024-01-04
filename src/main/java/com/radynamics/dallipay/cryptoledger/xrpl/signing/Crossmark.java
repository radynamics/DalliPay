package com.radynamics.dallipay.cryptoledger.xrpl.signing;

import com.radynamics.dallipay.browserwalletbridge.BrowserApiSubmitter;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterInfo;
import com.radynamics.dallipay.ui.Utils;

import java.net.URI;
import java.util.ResourceBundle;

public class Crossmark extends BrowserApiSubmitter {
    public final static String Id = "crossmark";

    public Crossmark(Ledger ledger) {
        super(ledger, Id, new com.radynamics.dallipay.browserwalletbridge.crossmark.Crossmark());
    }

    @Override
    protected TransactionSubmitterInfo createInfo() {
        final ResourceBundle res = ResourceBundle.getBundle("i18n.TransactionSubmitter");

        var info = new TransactionSubmitterInfo();
        info.setTitle(res.getString("crossmark.title"));
        info.setDescription(res.getString("crossmark.desc"));
        info.setDetailUri(URI.create("https://crossmark.io/"));
        info.setOrder(60);
        info.setIcon(Utils.getScaled("img/crossmark.png", 64, 64));
        return info;
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
    public boolean supportsPayload() {
        return true;
    }

    @Override
    public void deleteSettings() {
    }
}
