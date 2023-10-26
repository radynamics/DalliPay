package com.radynamics.dallipay.cryptoledger.xrpl.xahau;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterFactory;
import com.radynamics.dallipay.cryptoledger.xrpl.xahau.api.JsonRpcApi;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.exchange.ExchangeRateProviderFactory;
import com.radynamics.dallipay.exchange.ManualRateProvider;
import okhttp3.HttpUrl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public class Ledger extends com.radynamics.dallipay.cryptoledger.xrpl.Ledger {
    private final static Logger log = LogManager.getLogger(Ledger.class);
    public static final Integer NETWORKID_LIVENET = 21337;
    public static final Integer NETWORKID_TESTNET = 21338;

    @Override
    public LedgerId getId() {
        return LedgerId.Xahau;
    }

    @Override
    public String getNativeCcySymbol() {
        return "XAH";
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
    public FeeSuggestion getFeeSuggestion(Transaction t) {
        var api = new JsonRpcApi(this, getNetwork().getUrl());
        try {
            var fees = api.fee(t);
            return fees == null ? FeeSuggestion.None(getNativeCcySymbol()) : fees.createSuggestion();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return FeeSuggestion.None(getNativeCcySymbol());
        }
    }

    @Override
    public boolean equalTransactionFees() {
        // Fees vary depending on hooks installed on destination wallet
        return false;
    }

    @Override
    public NetworkInfo[] getDefaultNetworkInfo() {
        var networks = new NetworkInfo[2];
        networks[0] = NetworkInfo.createLivenet(HttpUrl.get("https://xahau.network/"), "Mainnet");
        networks[0].setNetworkId(NETWORKID_LIVENET);
        networks[1] = NetworkInfo.createTestnet(HttpUrl.get("https://xahau-test.net/"), "Testnet");
        networks[1].setNetworkId(NETWORKID_TESTNET);
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
        return HttpUrl.get("https://xahau-test.net");
    }

    @Override
    public PriceOracle[] getDefaultPriceOracles() {
        return new PriceOracle[0];
    }

    @Override
    public TransactionSubmitterFactory createTransactionSubmitterFactory() {
        return new com.radynamics.dallipay.cryptoledger.xrpl.xahau.TransactionSubmitterFactory(this);
    }
}
