package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Network;
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

    public Bithomp(Network network) {
        switch (network) {
            case Live -> {
                this.baseUrl = "https://www.bithomp.com/explorer/";
            }
            case Test -> {
                this.baseUrl = "https://test.bithomp.com/explorer/";
            }
            default -> throw new IllegalStateException("Unexpected value: " + network);
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
