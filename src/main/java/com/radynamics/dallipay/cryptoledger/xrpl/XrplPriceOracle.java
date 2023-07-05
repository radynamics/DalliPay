package com.radynamics.dallipay.cryptoledger.xrpl;

import com.google.common.primitives.UnsignedInteger;
import com.radynamics.dallipay.cryptoledger.Block;
import com.radynamics.dallipay.cryptoledger.BlockRange;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.cryptoledger.generic.Wallet;
import com.radynamics.dallipay.cryptoledger.xrpl.api.Convert;
import com.radynamics.dallipay.cryptoledger.xrpl.api.LedgerBlock;
import com.radynamics.dallipay.exchange.CurrencyPair;
import com.radynamics.dallipay.exchange.ExchangeRate;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    public ExchangeRate rateAt(CurrencyPair pair, Block block) {
        var targetCcy = pair.getFirstCode().equals("XRP") ? pair.getSecondCode() : pair.getFirstCode();
        var issuedCcy = issuedCurrencies.get(targetCcy);
        // Null when no oracle configuration is present for a given currency.
        if (issuedCcy == null) {
            return null;
        }

        var ledgerBlock = Convert.toLedgerBlock(block);
        var transactions = new com.radynamics.dallipay.cryptoledger.Transaction[0];
        try {
            var initialOffset = UnsignedInteger.valueOf(60 / Ledger.AVG_LEDGER_CLOSE_TIME_SEC); // around 1min
            transactions = loadTransactions(issuedCcy.getReceiver(), ledgerBlock, initialOffset, issuedCcy.getIssuer(), targetCcy);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        if (transactions.length == 0) {
            return null;
        }

        var bestMatch = getBestMatch(transactions, ledgerBlock);
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

    private com.radynamics.dallipay.cryptoledger.Transaction[] loadTransactions(Wallet receiver, LedgerBlock block, UnsignedInteger offset, Wallet issuer, String targetCcy) throws Exception {
        var period = BlockRange.of(block.minus(offset), block.plus(offset));
        var transactions = ledger.listTrustlineTransactions(receiver, period, issuer, targetCcy);

        final UnsignedInteger abortAtOffset = UnsignedInteger.valueOf(32 * 60 / Ledger.AVG_LEDGER_CLOSE_TIME_SEC); // around 32min
        if (transactions.length > 0 || offset.compareTo(abortAtOffset) > 0) {
            return transactions;
        }

        return loadTransactions(receiver, block, offset.times(UnsignedInteger.valueOf(2)), issuer, targetCcy);
    }

    private com.radynamics.dallipay.cryptoledger.Transaction getBestMatch(com.radynamics.dallipay.cryptoledger.Transaction[] transactions, LedgerBlock block) {
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
}
