package com.radynamics.dallipay.cryptoledger.bitcoin;

import com.radynamics.dallipay.cryptoledger.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.net.URI;
import java.util.Objects;

public class MempoolSpace implements WalletLookupProvider, TransactionLookupProvider {
    final static Logger log = LogManager.getLogger(MempoolSpace.class);
    private final String baseUrl;

    public static final String Id = "mempoolSpace";
    public static final String displayName = "mempool.space";

    public MempoolSpace(NetworkInfo network) throws LookupProviderException {
        if (network.getNetworkId() == null || Objects.equals(network.getNetworkId(), Ledger.NETWORKID_MAINNET)) {
            this.baseUrl = "https://mempool.space";
        } else if (Objects.equals(network.getNetworkId(), Ledger.NETWORKID_TESTNET)) {
            this.baseUrl = "https://mempool.space/testnet";
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
