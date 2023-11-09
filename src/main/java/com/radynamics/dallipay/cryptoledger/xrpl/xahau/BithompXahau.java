package com.radynamics.dallipay.cryptoledger.xrpl.xahau;

import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.xrpl.Ledger;
import com.radynamics.dallipay.ui.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class BithompXahau implements WalletLookupProvider, TransactionLookupProvider {
    final static Logger log = LogManager.getLogger(BithompXahau.class);
    private final String baseUrl;

    public static final String Id = "bithompXahau";
    public static final String displayName = "Bithomp";

    public BithompXahau(NetworkInfo network) throws LookupProviderException {
        if (Objects.equals(network.getNetworkId(), com.radynamics.dallipay.cryptoledger.xrpl.Ledger.NETWORKID_LIVENET)) {
            this.baseUrl = "https://www.xahauexplorer.com/explorer/";
        } else if (Objects.equals(network.getNetworkId(), com.radynamics.dallipay.cryptoledger.xrpl.xahau.Ledger.NETWORKID_TESTNET)) {
            this.baseUrl = "https://test.xahauexplorer.com/explorer/";
        } else {
            throw new LookupProviderException(String.format("%s doesn't support network %s.", displayName, network.getShortText()));
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
        try {
            Utils.openBrowser(null, new URI(String.format("%s%s", baseUrl, value)));
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
    }
}
