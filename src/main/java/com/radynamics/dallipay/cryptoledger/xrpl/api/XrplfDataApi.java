package com.radynamics.dallipay.cryptoledger.xrpl.api;

import com.google.common.primitives.UnsignedInteger;
import com.radynamics.dallipay.util.ApiRateLimitLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Access API to https://data.xrplf.org/docs/static/index.html.
 */
public class XrplfDataApi implements LedgerAtTimeProvider {
    private final static Logger log = LogManager.getLogger(XrplfDataApi.class);
    private final static ApiRateLimitLogger apiRateLimit = new ApiRateLimitLogger("XrplfDataApi");
    private final LedgerAtTimeCache cache = new LedgerAtTimeCache();
    private final LedgerRangeConverter fallback;

    public XrplfDataApi(LedgerRangeConverter fallback) {
        this.fallback = fallback;
    }

    @Override
    public Optional<LedgerAtTime> estimatedDaysAgo(long sinceDaysAgo) throws LedgerAtTimeException {
        return getLedgerIndexAt(ZonedDateTime.now().minusDays(sinceDaysAgo));
    }

    @Override
    public Optional<LedgerAtTime> at(ZonedDateTime dt) throws LedgerAtTimeException {
        return getLedgerIndexAt(dt);
    }

    private Optional<LedgerAtTime> getLedgerIndexAt(ZonedDateTime dt) throws LedgerAtTimeException {
        if (fallback != null && apiRateLimit.limited()) {
            return fallback.at(dt);
        }

        final var utc = ZoneId.of("UTC");
        var inUtc = dt.withZoneSameInstant(utc);
        log.trace(String.format("Find ledger at %s", inUtc));
        var ledger = cache.find(inUtc);
        if (ledger != null) {
            return Optional.of(ledger);
        }
        try {
            var json = get(new URL("https://data.xrplf.org/v1/ledgers/ledger_index?date=%s".formatted(inUtc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))));
            var closed = ZonedDateTime.parse(json.getString("closed")).withZoneSameInstant(utc);
            var ledgerIndex = LedgerIndex.of(UnsignedInteger.valueOf(json.getLong("ledger_index")));
            log.trace(String.format("ledgerIndex at %s: %s", closed, ledgerIndex));
            return Optional.of(cache.add(closed, ledgerIndex));
        } catch (IOException e) {
            throw new LedgerAtTimeException(e.getMessage(), e);
        }
    }

    private JSONObject get(URL url) throws IOException, LedgerAtTimeException {
        var conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(2000);
        conn.setRequestMethod("GET");
        conn.connect();

        apiRateLimit.log(conn.getHeaderFields());
        var responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new LedgerAtTimeException(String.format("Failed to load %s due HttpResponseCode %s", url, responseCode));
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
            return new JSONObject(sb.toString());
        } catch (JSONException e) {
            log.trace(e.getMessage(), e);
            return null;
        }
    }
}
