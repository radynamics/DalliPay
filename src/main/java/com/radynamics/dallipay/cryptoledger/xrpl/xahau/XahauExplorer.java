package com.radynamics.dallipay.cryptoledger.xrpl.xahau;

import com.radynamics.dallipay.cryptoledger.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.net.URI;

public class XahauExplorer implements WalletLookupProvider, TransactionLookupProvider {
    final static Logger log = LogManager.getLogger(XahauExplorer.class);
    private final String baseUrl;

    public static final String Id = "xahauExplorer";
    public static final String displayName = "Xahau Explorer";

    public XahauExplorer(NetworkInfo network) throws LookupProviderException {
        if (network.isLivenet()) {
            this.baseUrl = "https://explorer.xahau.net";
        } else if (network.isTestnet()) {
            this.baseUrl = "https://explorer.xahau-test.net";
        } else {
            throw new LookupProviderException(String.format("%s doesn't support network %s.", displayName, network.getShortText()));
        }
    }

    @Override
    public void open(Wallet wallet) {
        openInBrowser(null, wallet.getPublicKey());
    }

    @Override
    public void open(String transactionId) {
        openInBrowser("tx", transactionId);
    }

    private void openInBrowser(String suffix, String value) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                var url = suffix == null
                        ? String.format("%s/%s", baseUrl, value)
                        : String.format("%s/%s/%s", baseUrl, suffix, value);
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            log.warn("No desktop or no browsing supported");
        }
    }
}
