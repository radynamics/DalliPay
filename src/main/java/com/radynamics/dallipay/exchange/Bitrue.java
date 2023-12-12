package com.radynamics.dallipay.exchange;

import com.radynamics.dallipay.cryptoledger.Block;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.db.ConfigRepo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Bitrue implements ExchangeRateProvider {
    private final static Logger log = LogManager.getLogger(Bitrue.class);
    private final HashMap<String, CurrencyPair> currencyPairs = new HashMap<>();
    private final ArrayList<ExchangeRate> exchangeRates = new ArrayList<>();

    public static final String ID = "bitrue";

    public Bitrue(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        switch (ledger.getId()) {
            case Xahau -> currencyPairs.put("XAHUSDT", new CurrencyPair("XAH", "USD"));
            default -> throw new IllegalStateException("Unexpected value: " + ledger.getId());
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayText() {
        return "Bitrue";
    }

    @Override
    public CurrencyPair[] getSupportedPairs() {
        return currencyPairs.values().toArray(new CurrencyPair[0]);
    }

    @Override
    public boolean supportsRateAt() {
        return false;
    }

    @Override
    public void init(ConfigRepo repo) {
        // do nothing
    }

    @Override
    public void load() {
        exchangeRates.clear();
        for (var kvp : currencyPairs.entrySet()) {
            var symbol = kvp.getKey();
            try {
                var json = load(symbol);
                var ccyFrom = kvp.getValue().getFirstCode();
                var ccyTo = kvp.getValue().getSecondCode();
                exchangeRates.add(new ExchangeRate(ccyFrom, ccyTo, json.getDouble("askPrice"), ZonedDateTime.now()));
            } catch (Exception e) {
                log.error(String.format("Could not load rates for currencyPair %s", symbol), e);
            }
        }
    }

    private JSONObject load(String symbol) throws IOException, ExchangeException {
        var url = new URL(String.format("https://www.bitrue.com/api/v1/ticker/bookTicker?symbol=%s", symbol));

        var conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(2000);
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new ExchangeException(String.format("Failed to get exchange rate for %s from %s due HttpResponseCode %s", responseCode, symbol, getId()));
        }

        var sb = new StringBuilder();
        var scanner = new Scanner(url.openStream());
        while (scanner.hasNext()) {
            sb.append(scanner.nextLine());
        }
        scanner.close();

        return new JSONObject(sb.toString());
    }

    @Override
    public ExchangeRate[] latestRates() {
        return exchangeRates.toArray(new ExchangeRate[0]);
    }

    @Override
    public ExchangeRate rateAt(CurrencyPair pair, ZonedDateTime pointInTime, NetworkInfo blockNetwork, Block block) {
        return null;
    }
}
