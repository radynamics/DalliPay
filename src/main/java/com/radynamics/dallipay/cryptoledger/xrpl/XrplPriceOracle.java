package com.radynamics.dallipay.cryptoledger.xrpl;

import com.google.common.primitives.UnsignedInteger;
import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.Block;
import com.radynamics.dallipay.cryptoledger.BlockRange;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.Transaction;
import com.radynamics.dallipay.cryptoledger.generic.Wallet;
import com.radynamics.dallipay.cryptoledger.xrpl.api.Convert;
import com.radynamics.dallipay.cryptoledger.xrpl.api.LedgerBlock;
import com.radynamics.dallipay.exchange.CurrencyPair;
import com.radynamics.dallipay.exchange.ExchangeRate;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
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
    public ExchangeRate rateAt(CurrencyPair pair, ZonedDateTime pointInTime, NetworkInfo blockNetwork, Block block) {
        var targetCcy = pair.getFirstCode().equals("XRP") ? pair.getSecondCode() : pair.getFirstCode();
        var issuedCcy = issuedCurrencies.get(targetCcy);
        // Null when no oracle configuration is present for a given currency.
        if (issuedCcy == null) {
            return null;
        }

        LoadStrategy loader;
        if (blockNetwork.sameNet(ledger.getNetwork())) {
            // Same blockchain net -> load by block number
            loader = new BlockLoader(Convert.toLedgerBlock(block));
        } else {
            // Different blockchain net (eg. livenet/testnet) -> load by pointInTime due block numbers mismatch
            loader = new DateTimeLoader(pointInTime);
        }

        var transactions = new com.radynamics.dallipay.cryptoledger.Transaction[0];
        try {
            transactions = loader.loadTransactions(issuedCcy.getReceiver(), issuedCcy.getIssuer(), targetCcy);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        if (transactions.length == 0) {
            return null;
        }

        var bestMatch = loader.getBestMatch(transactions);
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

    private interface LoadStrategy {
        Transaction[] loadTransactions(Wallet receiver, Wallet issuer, String targetCcy) throws Exception;

        Transaction getBestMatch(Transaction[] transactions);
    }

    private class BlockLoader implements LoadStrategy {
        private final LedgerBlock block;

        public BlockLoader(LedgerBlock block) {
            this.block = block;
        }

        @Override
        public Transaction[] loadTransactions(Wallet receiver, Wallet issuer, String targetCcy) throws Exception {
            var initialOffset = UnsignedInteger.valueOf(60 / Ledger.AVG_LEDGER_CLOSE_TIME_SEC); // around 1min
            return loadTransactions(receiver, block, initialOffset, issuer, targetCcy);
        }

        @Override
        public Transaction getBestMatch(Transaction[] transactions) {
            var best = transactions[0];

            for (var t : transactions) {
                var gapBest = block.between(Convert.toLedgerBlock(best.getBlock()));
                var gap = block.between(Convert.toLedgerBlock(t.getBlock()));

                if (Math.abs(gap) < Math.abs(gapBest)) {
                    best = t;
                }
            }

            return best;
        }

        private com.radynamics.dallipay.cryptoledger.Transaction[] loadTransactions(Wallet receiver, LedgerBlock block, UnsignedInteger offset, Wallet issuer, String targetCcy) throws Exception {
            var period = BlockRange.of(block.minus(offset), block.plus(offset));
            var transactions = ledger.listTrustlineTransactions(receiver, period, issuer, targetCcy);

            final UnsignedInteger abortAtOffset = UnsignedInteger.valueOf(32 * 60 / Ledger.AVG_LEDGER_CLOSE_TIME_SEC); // around 32min
            if (transactions.length > 0 || offset.compareTo(abortAtOffset) > 0) {
                return transactions;
            }

            return loadTransactions(receiver, block, offset.times(UnsignedInteger.valueOf(2)), issuer, targetCcy);
        }
    }

    private class DateTimeLoader implements LoadStrategy {
        private final ZonedDateTime pointInTime;

        public DateTimeLoader(ZonedDateTime pointInTime) {
            this.pointInTime = pointInTime;
        }

        @Override
        public Transaction[] loadTransactions(Wallet receiver, Wallet issuer, String targetCcy) throws Exception {
            var initialOffsetMinutes = 1;
            return loadTransactions(receiver, pointInTime, initialOffsetMinutes, issuer, targetCcy);
        }

        @Override
        public Transaction getBestMatch(Transaction[] transactions) {
            var best = transactions[0];

            for (var t : transactions) {
                var gapBest = Duration.ofSeconds(ChronoUnit.SECONDS.between(pointInTime, best.getBooked()));
                var gap = Duration.ofSeconds(ChronoUnit.SECONDS.between(pointInTime, t.getBooked()));

                if (Math.abs(gap.toSeconds()) < Math.abs(gapBest.toSeconds())) {
                    best = t;
                }
            }

            return best;
        }

        private com.radynamics.dallipay.cryptoledger.Transaction[] loadTransactions(Wallet receiver, ZonedDateTime pointInTime, int offsetMinutes, Wallet issuer, String targetCcy) throws Exception {
            var period = DateTimeRange.of(pointInTime.minusMinutes(offsetMinutes), pointInTime.plusMinutes(offsetMinutes));
            var transactions = ledger.listTrustlineTransactions(receiver, period, issuer, targetCcy);

            final int abortAtOffset = 32;
            if (transactions.length > 0 || offsetMinutes > abortAtOffset) {
                return transactions;
            }

            return loadTransactions(receiver, pointInTime, offsetMinutes * 2, issuer, targetCcy);
        }
    }
}
