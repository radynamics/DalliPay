package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.TransactionLookupProvider;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletLookupProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.net.URI;

public class XrplOrg implements WalletLookupProvider, TransactionLookupProvider {
    private final static Logger log = LogManager.getLogger(XrplOrg.class);
    private final String baseUrl;

    public static final String Id = "xrplExplorer";
    public static final String displayName = "XRPL Explorer";

    public XrplOrg(NetworkInfo network) {
        if (network.isLivenet()) {
            this.baseUrl = "https://livenet.xrpl.org";
        } else if (network.isTestnet()) {
            this.baseUrl = "https://testnet.xrpl.org";
        } else {
            this.baseUrl = String.format("https://custom.xrpl.org/%s", network.getUrl().host());
        }
    }

    @Override
    public void open(Wallet wallet) {
        openInBrowser("accounts", wallet.getPublicKey());
    }

    @Override
    public void open(String transactionId) {
        openInBrowser("transactions", transactionId);
    }

    private void openInBrowser(String suffix, String value) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(String.format("%s/%s/%s", baseUrl, suffix, value)));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            log.warn("No desktop or no browsing supported");
        }
    }
}
