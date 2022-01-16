package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl;

import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyPair;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRate;
import com.radynamics.CryptoIso20022Interop.exchange.HistoricExchangeRateSource;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class XummPriceOracle implements HistoricExchangeRateSource {
    private final Ledger ledger;

    public static final String ID = "xummpriceoracle";

    public XummPriceOracle(Ledger ledger) {
        this.ledger = ledger;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayText() {
        return "XUMM Price Oracle";
    }

    @Override
    public CurrencyPair[] getSupportedPairs() {
        return new CurrencyPair[]{new CurrencyPair("XRP", "USD")};
    }

    @Override
    public ExchangeRate rateAt(CurrencyPair pair, LocalDateTime pointInTime) {
        if (!CurrencyPair.contains(getSupportedPairs(), pair)) {
            return null;
        }

        var period = DateTimeRange.of(pointInTime.minusMinutes(50), pointInTime.plusMinutes(50));
        Transaction[] transactions = new Transaction[0];
        try {
            transactions = ledger.listTransactions(new Wallet("rXUMMaPpZqPutoRszR29jtC8amWq3APkx"), period);
        } catch (Exception e) {
            LogManager.getLogger().error(e);
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
        return new ExchangeRate(pair, sum / rates.size(), bestMatch.getBooked());
    }

    private Transaction getBestMatch(Transaction[] transactions, LocalDateTime pointInTime) {
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
