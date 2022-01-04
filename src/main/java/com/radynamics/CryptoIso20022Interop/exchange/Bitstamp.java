package com.radynamics.CryptoIso20022Interop.exchange;

import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class Bitstamp implements Exchange {
    private static String[] currencyPairs = new String[]{"xrpusd", "xrpeur", "xrpgbp"};
    private ArrayList<ExchangeRate> exchangeRates = new ArrayList<>();

    @Override
    public String getId() {
        return "bitstamp";
    }

    @Override
    public void load() {
        exchangeRates.clear();
        for (var currencyPair : currencyPairs) {
            try {
                var json = load(currencyPair);
                var ccyFrom = currencyPair.substring(0, 3).toUpperCase();
                var ccyTo = currencyPair.substring(3).toUpperCase();
                exchangeRates.add(new ExchangeRate(ccyFrom, ccyTo, json.getDouble("last")));
            } catch (Exception e) {
                LogManager.getLogger().error(String.format("Could not load rates for currencyPair %s", currencyPair), e);
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
    public ExchangeRate[] rates() {
        return exchangeRates.toArray(new ExchangeRate[0]);
    }
}
