package com.radynamics.dallipay.exchange;

import com.radynamics.dallipay.cryptoledger.Ledger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;

public class Bitstamp implements ExchangeRateProvider {
    final static Logger log = LogManager.getLogger(Bitstamp.class);
    private CurrencyPair[] currencyPairs;
    private ArrayList<ExchangeRate> exchangeRates = new ArrayList<>();

    public static final String ID = "bitstamp";

    public Bitstamp(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        switch (ledger.getId()) {
            case Xrpl -> currencyPairs = new CurrencyPair[]{
                    new CurrencyPair("XRP", "USD"), new CurrencyPair("XRP", "EUR"), new CurrencyPair("XRP", "GBP")
            };
            case Ethereum -> currencyPairs = new CurrencyPair[]{
                    new CurrencyPair("ETH", "USD"), new CurrencyPair("ETH", "EUR"), new CurrencyPair("ETH", "GBP")
            };
            default -> throw new IllegalStateException("Unexpected value: " + ledger.getId());
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayText() {
        return "Bitstamp";
    }

    @Override
    public CurrencyPair[] getSupportedPairs() {
        return currencyPairs;
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
        for (var currencyPair : currencyPairs) {
            var key = String.format("%s%s", currencyPair.getFirst().getCode(), currencyPair.getSecond().getCode()).toLowerCase(Locale.ROOT);
            try {
                var json = load(key);
                var ccyFrom = key.substring(0, 3).toUpperCase();
                var ccyTo = key.substring(3).toUpperCase();
                var pointInTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(json.getLong("timestamp")), TimeZone.getDefault().toZoneId());
                exchangeRates.add(new ExchangeRate(ccyFrom, ccyTo, json.getDouble("last"), pointInTime));
            } catch (Exception e) {
                log.error(String.format("Could not load rates for currencyPair %s", key), e);
            }
        }
    }

    private JSONObject load(String currencyPair) throws IOException, ExchangeException {
        URL url = new URL(String.format("https://www.bitstamp.net/api/v2/ticker/%s/", currencyPair));

        var conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new ExchangeException(String.format("Failed to get exchange rate for %s from %s due HttpResponseCode %s", responseCode, currencyPair, getId()));
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
