package com.radynamics.dallipay.cryptoledger.xrpl.xahau;

import com.radynamics.dallipay.cryptoledger.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.net.URI;
import java.util.Objects;

public class XahauExplorer implements WalletLookupProvider, TransactionLookupProvider {
    final static Logger log = LogManager.getLogger(XahauExplorer.class);
    private final String baseUrl;

    public static final String Id = "xahauExplorer";
    public static final String displayName = "Xahau Explorer";

    public XahauExplorer(NetworkInfo network) throws LookupProviderException {
        if (Objects.equals(network.getNetworkId(), Ledger.NETWORKID_LIVENET)) {
            this.baseUrl = "https://explorer.xahau.net";
        } else if (Objects.equals(network.getNetworkId(), Ledger.NETWORKID_TESTNET)) {
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
