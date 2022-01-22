package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Network;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletLookupProvider;
import org.apache.logging.log4j.LogManager;

import java.awt.*;
import java.net.URI;

public class Bithomp implements WalletLookupProvider {
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
    public void open(String walletPublicKey) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(String.format("%s%s", baseUrl, walletPublicKey)));
            } catch (Exception e) {
                LogManager.getLogger().error(e.getMessage(), e);
            }
        } else {
            LogManager.getLogger().warn("No desktop or no browsing supported");
        }
    }
}
