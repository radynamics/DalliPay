package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.cryptoledger.NetworkInfo;
import com.radynamics.CryptoIso20022Interop.cryptoledger.TransactionLookupProvider;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletLookupProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.net.URI;

public class Bithomp implements WalletLookupProvider, TransactionLookupProvider {
    final static Logger log = LogManager.getLogger(Bithomp.class);
    private final String baseUrl;

    public static final String Id = "bithomp";
    public static final String displayName = "Bithomp";

    public Bithomp(NetworkInfo network) {
        if (network.isLivenet()) {
            this.baseUrl = "https://www.bithomp.com/explorer/";
        } else if (network.isTestnet()) {
            this.baseUrl = "https://test.bithomp.com/explorer/";
        } else {
            throw new IllegalStateException(String.format("%s doesn't support network %s.", displayName, network.getId()));
        }
    }

    @Override
    public void open(Wallet wallet) {
        openInBrowser(wallet.getPublicKey());
    }

    @Override
    public void open(String transactionId) {
        openInBrowser(transactionId);
    }

    private void openInBrowser(String value) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(String.format("%s%s", baseUrl, value)));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            log.warn("No desktop or no browsing supported");
        }
    }
}
