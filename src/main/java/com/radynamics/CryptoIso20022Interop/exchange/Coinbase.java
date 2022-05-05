package com.radynamics.CryptoIso20022Interop.exchange;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class Coinbase implements ExchangeRateProvider {
    final static Logger log = LogManager.getLogger(Coinbase.class);
    private final String[] baseCurrencies = new String[]{"xrp"};
    private final ArrayList<ExchangeRate> exchangeRates = new ArrayList<>();

    public static final String ID = "coinbase";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayText() {
        return "Coinbase";
    }

    @Override
    public CurrencyPair[] getSupportedPairs() {
        var pairs = new ArrayList<CurrencyPair>();
        for (var r : exchangeRates) {
            pairs.add(r.getPair());
        }
        return pairs.toArray(new CurrencyPair[0]);
    }

    @Override
    public boolean supportsRateAt() {
        return false;
    }

    @Override
    public void init() {
        // do nothing
    }

    @Override
    public void load() {
        exchangeRates.clear();
        for (var ledgerCcy : baseCurrencies) {
            try {
                var json = load(ledgerCcy);
                var data = json.getJSONObject("data");
                var ccyFrom = data.getString("currency").toUpperCase();
                var rates = data.getJSONObject("rates");
                for (var ccy : rates.keySet()) {
                    var ccyTo = ccy.toUpperCase();
                    var pointInTime = ZonedDateTime.now();
                    exchangeRates.add(new ExchangeRate(ccyFrom, ccyTo, rates.getDouble(ccy), pointInTime));
                }
            } catch (Exception e) {
                log.error(String.format("Could not load rates for currency %s", ledgerCcy), e);
            }
        }
    }

    private JSONObject load(String ledgerCcy) throws IOException, ExchangeException {
        var url = new URL(String.format("https://api.coinbase.com/v2/exchange-rates?currency=%s", ledgerCcy));

        var conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new ExchangeException(String.format("Failed to get exchange rate for %s from %s due HttpResponseCode %s", responseCode, ledgerCcy, getId()));
        }

        var responseString = "";
        var scanner = new Scanner(url.openStream());
        while (scanner.hasNext()) {
            responseString += scanner.nextLine();
        }
        scanner.close();

        return new JSONObject(responseString);
    }

    @Override
    public ExchangeRate[] latestRates() {
        return exchangeRates.toArray(new ExchangeRate[0]);
    }

    @Override
    public ExchangeRate rateAt(CurrencyPair pair, ZonedDateTime pointInTime) {
        return null;
    }
}
