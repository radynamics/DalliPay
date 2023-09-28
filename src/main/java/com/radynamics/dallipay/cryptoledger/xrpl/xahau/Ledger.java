package com.radynamics.dallipay.cryptoledger.xrpl.xahau;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.dallipay.cryptoledger.FeeSuggestion;
import com.radynamics.dallipay.cryptoledger.LedgerId;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.Transaction;
import com.radynamics.dallipay.cryptoledger.xrpl.xahau.api.WebSocketApi;
import com.radynamics.dallipay.exchange.*;
import okhttp3.HttpUrl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import javax.swing.*;
import java.net.URI;

public class Ledger extends com.radynamics.dallipay.cryptoledger.xrpl.Ledger {
    private final static Logger log = LogManager.getLogger(Ledger.class);

    private static final String nativeCcySymbol = "XRP+";

    @Override
    public LedgerId getId() {
        return LedgerId.Xahau;
    }

    @Override
    public String getNativeCcySymbol() {
        return nativeCcySymbol;
    }

    @Override
    public Icon getIcon() {
        return new FlatSVGIcon("svg/xahau.svg", 16, 16);
    }

    @Override
    public String getDisplayText() {
        return "Xahau";
    }

    static Money dropsToXrpPlus(long drops) {
        return Money.of(XrpCurrencyAmount.ofDrops(drops).toXrp().doubleValue(), new Currency(nativeCcySymbol));
    }

    @Override
    public FeeSuggestion getFeeSuggestion() {
        // TODO: implement
        return super.getFeeSuggestion();
    }

    public FeeSuggestion getFeeSuggestion(Transaction t) {
        var api = new WebSocketApi(toWebSocketUri(getNetwork().getUrl().uri()));
        try {
            var fees = api.fee(t);
            return fees == null ? FeeSuggestion.None(getNativeCcySymbol()) : fees.createSuggestion();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return FeeSuggestion.None(getNativeCcySymbol());
        }
    }

    private URI toWebSocketUri(URI uri) {
        // TODO: Hack
        return URI.create(uri.toString().replace("https://", "wss://"));
    }

    @Override
    public NetworkInfo[] getDefaultNetworkInfo() {
        var networks = new NetworkInfo[2];
        networks[0] = NetworkInfo.createLivenet(HttpUrl.get("https://xahau.network/"), "Mainnet");
        networks[1] = NetworkInfo.createTestnet(HttpUrl.get("https://xahau-test.net/"), "Testnet");
        return networks;
    }

    @Override
    public String[] getExchangeRateProviders() {
        return new String[]{ManualRateProvider.ID};
    }

    @Override
    public ExchangeRateProvider getDefaultExchangeRateProvider() {
        return ExchangeRateProviderFactory.create(ManualRateProvider.ID, this);
    }

    @Override
    public HttpUrl getDefaultFaucetUrl() {
        return HttpUrl.get("https://xahau-test.net/faucet");
    }
}
