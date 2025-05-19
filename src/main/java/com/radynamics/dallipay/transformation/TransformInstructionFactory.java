package com.radynamics.dallipay.transformation;

import com.radynamics.dallipay.Config;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.NetworkInfoFactory;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.exchange.Coinbase;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.exchange.ExchangeRateProviderFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class TransformInstructionFactory {
    private final static Logger log = LogManager.getLogger(TransformInstructionFactory.class);

    public static TransformInstruction create(Ledger ledger, String configFilePath, String networkId) {
        var config = Config.loadOrFallback(ledger, configFilePath);
        var network = NetworkInfoFactory.getOrDefault(ledger, config, networkId);
        return create(ledger, config, network);
    }

    public static TransformInstruction create(Ledger ledger, Config config, NetworkInfo network) {
        var t = new TransformInstruction(ledger, config, new DbAccountMappingSource(ledger));
        t.setNetwork(network);
        try (var repo = new ConfigRepo()) {
            var persistedProvider = repo.getExchangeRateProvider();
            t.setExchangeRateProvider(createExchangeRateProvider(ledger, persistedProvider.orElse(null)));
            t.getExchangeRateProvider().init(repo);
            // Different ledgers/sidechains may provide different sources for historic exchange rates.
            var historicRateSource = repo.getHistoricExchangeRateSource(ledger)
                    .map(s -> ExchangeRateProviderFactory.create(s, ledger))
                    .orElseGet(() -> ExchangeRateProviderFactory.defaultPriceOracle(ledger));
            t.setHistoricExchangeRateSource(historicRateSource);
            t.getHistoricExchangeRateSource().init(repo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            t.setExchangeRateProvider(ExchangeRateProviderFactory.create(Coinbase.ID, ledger));
        }
        return t;
    }

    private static ExchangeRateProvider createExchangeRateProvider(Ledger ledger, String id) {
        if (id == null || !ExchangeRateProviderFactory.supports(ledger, id)) {
            return ledger.getDefaultExchangeRateProvider();
        }

        return ExchangeRateProviderFactory.create(id, ledger);
    }
}
