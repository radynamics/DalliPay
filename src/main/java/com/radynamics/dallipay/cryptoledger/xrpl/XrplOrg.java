package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.TransactionLookupProvider;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletLookupProvider;
import com.radynamics.dallipay.ui.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class XrplOrg implements WalletLookupProvider, TransactionLookupProvider {
    private final static Logger log = LogManager.getLogger(XrplOrg.class);
    private final String baseUrl;

    public static final String Id = "xrplExplorer";
    public static final String displayName = "XRPL Explorer";

    public XrplOrg(NetworkInfo network) {
        if (Objects.equals(network.getNetworkId(), Ledger.NETWORKID_LIVENET)) {
            this.baseUrl = "https://livenet.xrpl.org";
        } else if (Objects.equals(network.getNetworkId(), Ledger.NETWORKID_TESTNET)) {
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
        try {
            Utils.openBrowser(null, new URI(String.format("%s/%s/%s", baseUrl, suffix, value)));
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
    }
}
