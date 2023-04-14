package com.radynamics.dallipay.cryptoledger.ethereum;

import com.radynamics.dallipay.cryptoledger.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.net.URI;

public class Etherscan implements WalletLookupProvider, TransactionLookupProvider {
    final static Logger log = LogManager.getLogger(Etherscan.class);
    private final String baseUrl;

    public static final String Id = "etherscan";
    public static final String displayName = "Etherscan";

    public Etherscan(NetworkInfo network) throws LookupProviderException {
        if (network.isLivenet()) {
            this.baseUrl = "https://etherscan.io/";
        } else if (network.isTestnet()) {
            this.baseUrl = "https://goerli.etherscan.io/";
        } else {
            throw new LookupProviderException(String.format("%s doesn't support network %s.", displayName, network.getShortText()));
        }
    }

    @Override
    public void open(Wallet wallet) {
        openInBrowser("address", wallet.getPublicKey());
    }

    @Override
    public void open(String transactionId) {
        openInBrowser("tx", transactionId);
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
