package com.radynamics.dallipay.cryptoledger.xrpl.signing;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.dallipay.browserwalletbridge.BrowserApiSubmitter;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterInfo;

import java.net.URI;
import java.util.ResourceBundle;

public class GemWallet extends BrowserApiSubmitter {
    public final static String Id = "gemWallet";

    public GemWallet(Ledger ledger) {
        super(ledger, Id, new com.radynamics.dallipay.browserwalletbridge.gemwallet.GemWallet());
    }

    @Override
    protected TransactionSubmitterInfo createInfo() {
        final ResourceBundle res = ResourceBundle.getBundle("i18n.TransactionSubmitter");

        var info = new TransactionSubmitterInfo();
        info.setTitle(res.getString("gemwallet.title"));
        info.setDescription(res.getString("gemwallet.desc"));
        info.setDetailUri(URI.create("https://gemwallet.app"));
        info.setOrder(50);
        info.setIcon(new FlatSVGIcon("img/gemwallet.svg", 64, 64));
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
