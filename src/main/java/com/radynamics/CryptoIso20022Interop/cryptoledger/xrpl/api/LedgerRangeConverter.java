package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api;

import com.google.common.primitives.UnsignedInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class LedgerRangeConverter {
    final static Logger log = LogManager.getLogger(LedgerRangeConverter.class);
    private XrplClient xrplClient;
    private final LedgerAtTimeCache cache = new LedgerAtTimeCache();

    public LedgerRangeConverter(XrplClient xrplClient) {
        this.xrplClient = xrplClient;
    }

    public LedgerIndex findOrNull(ZonedDateTime dt) throws JsonRpcClientErrorException {
        var ledger = findLedger(dt);
        return ledger == null ? null : ledger.getLedgerIndex();
    }

    private LedgerAtTime findLedger(ZonedDateTime dt) throws JsonRpcClientErrorException {
        log.trace(String.format("Find ledger at %s", dt));
        var latestLedger = cache.find(dt);
        if (latestLedger == null) {
            var latestLedgerResult = xrplClient.ledger(LedgerRequestParams.builder().ledgerSpecifier(LedgerSpecifier.CLOSED).build());
            latestLedger = cache.add(latestLedgerResult.ledger().closeTimeHuman().get(), latestLedgerResult.ledgerIndexSafe());
        }
        if (dt.isAfter(latestLedger.getPointInTime())) {
            log.trace(String.format("%s is after last ledger -> take last ledger at %s", dt, latestLedger.getPointInTime()));
            return latestLedger;
        }

        var avgDurationPerLedger = getAverageLedgerDuration(latestLedger);

        int iteration = 0;
        final int maxIterations = 20;
        var bestMatch = latestLedger;
        while (!isLedgerAt(bestMatch, dt) && iteration < maxIterations) {
            var fromDifference = Duration.ofMillis(ChronoUnit.MILLIS.between(dt, bestMatch.getPointInTime()));
            var isTooEarly = fromDifference.isNegative();
            var estimatedOffsetSecods = fromDifference.toMillis() / (double) avgDurationPerLedger.toMillis();
            // ceil: if last found is slightly too early just take the next one.
            var estimatedOffset = UnsignedInteger.valueOf(Math.abs(Math.round(Math.ceil(estimatedOffsetSecods))));
            if (estimatedOffset.equals(UnsignedInteger.ZERO)) {
                break;
            }
            var estimatedFromLedgerIndex = isTooEarly
                    ? bestMatch.getLedgerIndex().plus(estimatedOffset)
                    : bestMatch.getLedgerIndex().minus(estimatedOffset);

            var tmp = get(estimatedFromLedgerIndex, !isTooEarly);
            if (tmp == null) {
                log.warn(String.format("Failed to get ledger index near %s.", estimatedFromLedgerIndex));
                return null;
            }
            bestMatch = tmp;

            log.trace(String.format("Iteration %s: estOffset %s -> %s", iteration, estimatedOffset, bestMatch.getPointInTime()));
            iteration++;
        }

        return bestMatch;
    }

    private LedgerAtTime get(LedgerIndex index, boolean searchEarlier) {
        var indexCandidate = index;

        final int Max = 10;
        for (var i = 0; i < Max; i++) {
            var candidate = get(indexCandidate);
            if (candidate != null) {
                return candidate;
            }

            var offset = UnsignedInteger.valueOf(100);
            indexCandidate = searchEarlier ? indexCandidate.minus(offset) : indexCandidate.plus(offset);
            log.trace(String.format("Ledger index %s not found, looking for %s instead", indexCandidate, indexCandidate));
        }

        return null;
    }

    private LedgerAtTime get(LedgerIndex index) {
        var ledger = cache.find(index);
        if (ledger != null) {
            return ledger;
        }

        try {
            var ledgerResult = xrplClient.ledger(LedgerRequestParams.builder().ledgerSpecifier(LedgerSpecifier.of(index)).build());
            return cache.add(ledgerResult.ledger().closeTimeHuman().get(), ledgerResult.ledgerIndexSafe());
        } catch (JsonRpcClientErrorException e) {
            if (!e.getMessage().equalsIgnoreCase("ledgerNotFound")) {
                log.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    private Duration getAverageLedgerDuration(LedgerAtTime ledger) {
        return getAverageLedgerDuration(ledger, UnsignedInteger.valueOf(1000));
    }

    private Duration getAverageLedgerDuration(LedgerAtTime ledger, UnsignedInteger referenceInterval) {
        var referenceLedgerIndex = ledger.getLedgerIndex().minus(referenceInterval);
        var referenceEarlier = get(referenceLedgerIndex);
        if (referenceEarlier == null) {
            var changedReferenceInterval = referenceInterval.minus(UnsignedInteger.valueOf(100));
            log.trace(String.format("Ledger index %s not found, changing reference interval from %s to %s.", referenceEarlier, referenceInterval, changedReferenceInterval));
            return getAverageLedgerDuration(ledger, changedReferenceInterval);
        }

        var durationSeconds = ChronoUnit.SECONDS.between(ledger.getPointInTime(), referenceEarlier.getPointInTime());
        var avgDurationPerLedger = durationSeconds / (double) referenceInterval.longValue();
        return Duration.ofMillis(Math.round(avgDurationPerLedger * 1000));
    }

    private boolean isLedgerAt(LedgerAtTime ledgerAtTime, ZonedDateTime dt) {
        var closeTime = ledgerAtTime.getPointInTime();
        var diff = ChronoUnit.SECONDS.between(closeTime, dt);
        // accept ledger within a smaller timeframe.
        return 0 < diff && diff < Duration.ofSeconds(60).getSeconds();
    }
}
