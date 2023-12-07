package com.radynamics.dallipay.cryptoledger.xrpl.xahau;

import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.ui.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class XahScan implements WalletLookupProvider, TransactionLookupProvider {
    private final static Logger log = LogManager.getLogger(XahScan.class);
    private final String baseUrl = "https://xahscan.com";

    public static final String Id = "xahscan";
    public static final String displayName = "XAH SCAN";

    public XahScan(NetworkInfo network) throws LookupProviderException {
        if (!Objects.equals(network.getNetworkId(), com.radynamics.dallipay.cryptoledger.xrpl.xahau.Ledger.NETWORKID_LIVENET)) {
            throw new LookupProviderException(String.format("%s doesn't support network %s.", XahScan.displayName, network.getShortText()));
        }
    }

    @Override
    public void open(Wallet wallet) {
        openInBrowser("account", wallet.getPublicKey());
    }

    @Override
    public void open(String transactionId) {
        openInBrowser("tx", transactionId);
    }

    private void openInBrowser(String suffix, String value) {
        try {
            Utils.openBrowser(null, new URI(String.format("%s/%s/%s", baseUrl, suffix, value)));
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
    }
}
