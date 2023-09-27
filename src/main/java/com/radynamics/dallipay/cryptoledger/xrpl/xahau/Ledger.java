package com.radynamics.dallipay.cryptoledger.xrpl.xahau;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.dallipay.cryptoledger.FeeSuggestion;
import com.radynamics.dallipay.cryptoledger.LedgerId;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.exchange.ExchangeRateProviderFactory;
import com.radynamics.dallipay.exchange.ManualRateProvider;
import okhttp3.HttpUrl;

import javax.swing.*;

public class Ledger extends com.radynamics.dallipay.cryptoledger.xrpl.Ledger {
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

    @Override
    public FeeSuggestion getFeeSuggestion() {
        // TODO: implement
        return super.getFeeSuggestion();
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
