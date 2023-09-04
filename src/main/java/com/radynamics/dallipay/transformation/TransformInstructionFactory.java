package com.radynamics.dallipay.transformation;

import com.radynamics.dallipay.Config;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.exchange.Coinbase;
import com.radynamics.dallipay.exchange.ExchangeRateProviderFactory;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

public final class TransformInstructionFactory {
    private final static Logger log = LogManager.getLogger(TransformInstructionFactory.class);

    public static TransformInstruction create(Ledger ledger, String configFilePath, String networkId) {
        var config = Config.loadOrFallback(ledger, configFilePath);
        var t = new TransformInstruction(ledger, config, new DbAccountMappingSource(ledger.getId()));
        t.setNetwork(getNetworkOrDefault(ledger, config, networkId));
        try (var repo = new ConfigRepo()) {
            t.setExchangeRateProvider(ExchangeRateProviderFactory.create(repo.getExchangeRateProvider(), ledger));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            t.setExchangeRateProvider(ExchangeRateProviderFactory.create(Coinbase.ID, ledger));
        }
        t.getExchangeRateProvider().init();

        // Different ledgers/sidechains may provide different sources for historic exchange rates.
        t.setHistoricExchangeRateSource(ledger.createHistoricExchangeRateSource());
        t.getHistoricExchangeRateSource().init();
        return t;
    }

    private static NetworkInfo getNetworkOrDefault(Ledger ledger, Config config, String networkId) {
        if (!StringUtils.isEmpty(networkId)) {
            var networkByParam = config.getNetwork(networkId.toLowerCase(Locale.ROOT));
            if (networkByParam.isPresent()) {
                return networkByParam.get();
            }
        }

        HttpUrl lastUsed = null;
        try (var repo = new ConfigRepo()) {
            lastUsed = repo.getLastUsedRpcUrl(ledger);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (lastUsed == null) {
            return config.getDefaultNetworkInfo();
        }

        for (var ni : config.getNetworkInfos()) {
            if (ni.getUrl().equals(lastUsed)) {
                return ni;
            }
        }

        return NetworkInfo.create(lastUsed, lastUsed.toString());
    }
}
