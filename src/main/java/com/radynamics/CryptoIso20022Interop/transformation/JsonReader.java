package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.Config;
import com.radynamics.CryptoIso20022Interop.cryptoledger.LedgerFactory;
import com.radynamics.CryptoIso20022Interop.cryptoledger.LedgerId;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Network;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.XrplPriceOracle;
import com.radynamics.CryptoIso20022Interop.db.ConfigRepo;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRateProvider;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRateProviderFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonReader {
    final static Logger log = LogManager.getLogger(JsonReader.class);

    public TransformInstruction read(InputStream input, String configFilePath, Network network) {
        var bufferedReader = new BufferedReader(new InputStreamReader(input));
        var tokener = new JSONTokener(bufferedReader);
        var json = new JSONObject(tokener);

        // TODO: validate format
        var ledger = LedgerFactory.create(LedgerId.Xrpl);
        Config config = Config.load(ledger, configFilePath);
        ledger.setNetwork(config.getNetwork(network));
        var ti = new TransformInstruction(ledger);
        ti.setExchangeRateProvider(getExchangeRateProvider());
        ti.getExchangeRateProvider().init();
        ti.setHistoricExchangeRateSource(ExchangeRateProviderFactory.create(XrplPriceOracle.ID, config.getNetwork(Network.Live)));
        ti.getHistoricExchangeRateSource().init();

        return ti;
    }

    private ExchangeRateProvider getExchangeRateProvider() {
        String id = null;
        try (var repo = new ConfigRepo()) {
            id = repo.getExchangeRateProvider();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return ExchangeRateProviderFactory.create(id);
    }
}
