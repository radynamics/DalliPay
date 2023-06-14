package com.radynamics.dallipay.cryptoledger.ethereum.signing;

import com.radynamics.dallipay.browserwalletbridge.BrowserApiSubmitter;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterInfo;
import com.radynamics.dallipay.ui.Utils;

import java.net.URI;
import java.util.ResourceBundle;

public class MetaMask extends BrowserApiSubmitter {
    public final static String Id = "metaMask";

    public MetaMask(Ledger ledger) {
        super(ledger, Id, new com.radynamics.dallipay.browserwalletbridge.metamask.MetaMask());
    }

    @Override
    protected TransactionSubmitterInfo createInfo() {
        final ResourceBundle res = ResourceBundle.getBundle("i18n.TransactionSubmitter");

        var info = new TransactionSubmitterInfo();
        info.setTitle(res.getString("metamask.title"));
        info.setDescription(res.getString("metamask.desc"));
        info.setDetailUri(URI.create("https://metamask.io"));
        info.setOrder(50);
        info.setIcon(Utils.getScaled("img/metamask.png", 64, 64));
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
}
