package com.radynamics.CryptoIso20022Interop.exchange;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TimeZone;

public class Bitstamp implements ExchangeRateProvider {
    final static Logger log = LogManager.getLogger(Bitstamp.class);
    private static String[] currencyPairs = new String[]{"xrpusd", "xrpeur", "xrpgbp"};
    private ArrayList<ExchangeRate> exchangeRates = new ArrayList<>();

    public static final String ID = "bitstamp";

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
        return new CurrencyPair[]{
                new CurrencyPair("XRP", "USD"), new CurrencyPair("XRP", "EUR"), new CurrencyPair("XRP", "GBP")
        };
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
            try {
                var json = load(currencyPair);
                var ccyFrom = currencyPair.substring(0, 3).toUpperCase();
                var ccyTo = currencyPair.substring(3).toUpperCase();
                var pointInTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(json.getLong("timestamp")), TimeZone.getDefault().toZoneId());
                exchangeRates.add(new ExchangeRate(ccyFrom, ccyTo, json.getDouble("last"), pointInTime));
            } catch (Exception e) {
                log.error(String.format("Could not load rates for currencyPair %s", currencyPair), e);
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
