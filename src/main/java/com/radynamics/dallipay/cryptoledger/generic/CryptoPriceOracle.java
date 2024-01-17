package com.radynamics.dallipay.cryptoledger.generic;

import com.radynamics.dallipay.DateTimeConvert;
import com.radynamics.dallipay.Secrets;
import com.radynamics.dallipay.cryptoledger.Block;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.exchange.*;
import okhttp3.HttpUrl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class CryptoPriceOracle implements ExchangeRateProvider {
    private final static Logger log = LogManager.getLogger(CryptoPriceOracle.class);
    private final Currency base;
    private HttpUrl url;
    private String apiKey;
    private final CurrencyPair[] currencyPairs = new CurrencyPair[]{
            new CurrencyPair("XRP", "USD"), new CurrencyPair("XRP", "EUR"), new CurrencyPair("XRP", "JPY"), new CurrencyPair("XRP", "KRW"),
            new CurrencyPair("XRP", "TRY"), new CurrencyPair("XRP", "GBP"), new CurrencyPair("XRP", "THB"), new CurrencyPair("XRP", "RUB"),
            new CurrencyPair("XRP", "BRL"), new CurrencyPair("XRP", "AUD"), new CurrencyPair("XRP", "MXN"), new CurrencyPair("XRP", "ZAR"),
            new CurrencyPair("XRP", "MYR"), new CurrencyPair("XRP", "IDR"), new CurrencyPair("XRP", "SGD"), new CurrencyPair("XRP", "CHF"),
            new CurrencyPair("XAH", "USD"),
            new CurrencyPair("BTC", "USD"), new CurrencyPair("BTC", "EUR"),
    };

    public static final String ID = "cryptopriceoracle";

    public CryptoPriceOracle(Currency base) {
        this.base = base;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayText() {
        return "Crypto Price Oracle";
    }

    @Override
    public CurrencyPair[] getSupportedPairs() {
        return Arrays.stream(currencyPairs).filter(o -> o.getFirst().sameCode(base)).toArray(CurrencyPair[]::new);
    }

    @Override
    public boolean supportsRateAt() {
        return true;
    }

    @Override
    public void init(ConfigRepo repo) {
        var fallback = HttpUrl.get("https://priceoracle.radynamics.com/api/");
        try {
            url = repo.getCryptoPriceOracleUrl().orElse(fallback);
            apiKey = Secrets.getCryptoPriceOracleApiKey(repo);
            if (apiKey == null) {
                throw new RuntimeException("No apiKey for CryptoPriceOracle available.");
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            url = fallback;
        }
    }

    @Override
    public void load() {
        // do nothing
    }

    @Override
    public ExchangeRate[] latestRates() {
        return new ExchangeRate[0];
    }

    @Override
    public ExchangeRate rateAt(CurrencyPair pair, ZonedDateTime pointInTime, NetworkInfo blockNetwork, Block block) {
        try {
            var data = load(pair, pointInTime);
            if (data == null) {
                return null;
            }

            var at = DateTimeConvert.toUserTimeZone(ZonedDateTime.parse(data.getString("at")));
            return new ExchangeRate(pair, data.getDouble("rate"), at);
        } catch (IOException | ExchangeException e) {
            log.info(e.getMessage(), e);
            return null;
        }
    }

    private URL createUrl(CurrencyPair pair, ZonedDateTime pointInTime) throws MalformedURLException {
        // Eg. "http://localhost:3000/rate/XRP?quote=USD&at=2023-12-02T114150Z"
        var utcTime = pointInTime.withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        var sb = new StringBuilder();
        sb.append(this.url.toString());
        if (!sb.toString().endsWith("/")) {
            sb.append("/");
        }
        sb.append("rate/%s?quote=%s&at=%s".formatted(pair.getFirstCode(), pair.getSecondCode(), utcTime));
        return new URL(sb.toString());
    }

    private JSONObject load(CurrencyPair pair, ZonedDateTime pointInTime) throws IOException, ExchangeException {
        var url = createUrl(pair, pointInTime);

        var conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("x-api-key", apiKey);
        conn.connect();

        var responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new ExchangeException(String.format("Failed to get historic price for %s due HttpResponseCode %s", pair.getDisplayText(), responseCode));
        }

        var br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        var sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            if (sb.length() > 0) {
                sb.append(System.lineSeparator());
            }
            sb.append(line);
        }

        try {
            var result = new JSONObject(sb.toString());
            if (!result.getBoolean("success")) {
                throw new ExchangeException(result.getJSONObject("error").getString("message"));
            }
            return result.optJSONObject("data");
        } catch (JSONException e) {
            log.trace(e.getMessage(), e);
            return null;
        }
    }
}
