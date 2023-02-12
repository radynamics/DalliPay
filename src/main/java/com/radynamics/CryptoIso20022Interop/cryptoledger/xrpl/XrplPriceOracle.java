package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.cryptoledger.NetworkInfo;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyPair;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRateProvider;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

public class XrplPriceOracle implements ExchangeRateProvider {
    final static Logger log = LogManager.getLogger(XrplPriceOracle.class);
    private final Ledger ledger;
    private final HashMap<String, IssuedCurrency> issuedCurrencies = new HashMap<>();

    public static final String ID = "xrplpriceoracle";

    public XrplPriceOracle(NetworkInfo network) {
        ledger = new Ledger();
        ledger.setNetwork(network);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayText() {
        return "XRPL Price Oracle";
    }

    @Override
    public CurrencyPair[] getSupportedPairs() {
        var list = new ArrayList<CurrencyPair>();
        for (var kvp : issuedCurrencies.entrySet()) {
            list.add(kvp.getValue().getPair());
        }
        return list.toArray(list.toArray(new CurrencyPair[0]));
    }

    @Override
    public boolean supportsRateAt() {
        return true;
    }

    @Override
    public void init() {
        issuedCurrencies.clear();

        var config = new XrplPriceOracleConfig();
        config.load();
        for (var o : config.issuedCurrencies()) {
            var nonLedgerCcy = o.getPair().getFirstCode().equals(ledger.getNativeCcySymbol()) ? o.getPair().getSecondCode() : o.getPair().getFirstCode();
            issuedCurrencies.put(nonLedgerCcy, o);
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
    public ExchangeRate rateAt(CurrencyPair pair, ZonedDateTime pointInTime) {
        var targetCcy = pair.getFirstCode().equals("XRP") ? pair.getSecondCode() : pair.getFirstCode();
        var issuedCcy = issuedCurrencies.get(targetCcy);
        // Null when no oracle configuration is present for a given currency.
        if (issuedCcy == null) {
            return null;
        }

        Transaction[] transactions = new Transaction[0];
        try {
            var initialOffsetMinutes = 1;
            transactions = loadTransactions(issuedCcy.getReceiver(), pointInTime, initialOffsetMinutes, issuedCcy.getIssuer(), targetCcy);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (transactions.length == 0) {
            return null;
        }

        var bestMatch = getBestMatch(transactions, pointInTime);
        var rates = new ArrayList<Double>();
        for (var m : bestMatch.getMessages()) {
            var ratesText = m.split(";");
            if (ratesText.length == 0 || !NumberUtils.isCreatable(ratesText[0])) {
                continue;
            }
            rates.add(Double.valueOf(ratesText[0]));
        }

        if (rates.size() == 0) {
            return null;
        }

        var sum = Double.valueOf(0);
        for (var r : rates) {
            sum += r;
        }

        final double PRECISION = 100000d;
        var rate = Math.round(sum / rates.size() * PRECISION) / PRECISION;
        return new ExchangeRate(pair, rate, bestMatch.getBooked());
    }

    private Transaction[] loadTransactions(Wallet receiver, ZonedDateTime pointInTime, int offsetMinutes, Wallet issuer, String targetCcy) throws Exception {
        var period = DateTimeRange.of(pointInTime.minusMinutes(offsetMinutes), pointInTime.plusMinutes(offsetMinutes));
        var transactions = ledger.listTrustlineTransactions(receiver, period, issuer, targetCcy);

        final int abortAtOffset = 32;
        if (transactions.length > 0 || offsetMinutes > abortAtOffset) {
            return transactions;
        }

        return loadTransactions(receiver, pointInTime, offsetMinutes * 2, issuer, targetCcy);
    }

    private Transaction getBestMatch(Transaction[] transactions, ZonedDateTime pointInTime) {
        Transaction best = transactions[0];

        for (var t : transactions) {
            var gapBest = Duration.ofSeconds(ChronoUnit.SECONDS.between(pointInTime, best.getBooked()));
            var gap = Duration.ofSeconds(ChronoUnit.SECONDS.between(pointInTime, t.getBooked()));

            if (Math.abs(gap.toSeconds()) < Math.abs(gapBest.toSeconds())) {
                best = t;
            }
        }

        return best;
    }
}
