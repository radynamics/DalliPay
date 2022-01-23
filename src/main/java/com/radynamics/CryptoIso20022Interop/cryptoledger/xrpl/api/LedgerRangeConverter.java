package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api;

import com.google.common.primitives.UnsignedInteger;
import com.radynamics.CryptoIso20022Interop.DateTimeConvert;
import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class LedgerRangeConverter {
    private final Logger logger;
    private XrplClient xrplClient;
    private final LedgerAtTimeCache cache = new LedgerAtTimeCache();

    public LedgerRangeConverter(XrplClient xrplClient) {
        this.xrplClient = xrplClient;
        this.logger = LogManager.getLogger();
    }

    public LedgerIndexRange convert(DateTimeRange period) throws JsonRpcClientErrorException {
        var ledgerAtStart = findLedger(period.getStart());
        var ledgerAtEnd = findLedger(period.getEnd());

        logger.trace(String.format("RESULT: %s - %s", ledgerAtStart.getPointInTime(), ledgerAtEnd.getPointInTime()));
        return LedgerIndexRange.of(ledgerAtStart.getLedgerIndex(), ledgerAtEnd.getLedgerIndex());
    }

    private LedgerAtTime findLedger(LocalDateTime dt) throws JsonRpcClientErrorException {
        logger.trace(String.format("Find ledger at %s", dt));
        var latestLedger = cache.find(dt);
        if (latestLedger == null) {
            var latestLedgerResult = xrplClient.ledger(LedgerRequestParams.builder().ledgerSpecifier(LedgerSpecifier.CLOSED).build());
            latestLedger = cache.add(DateTimeConvert.toLocal(latestLedgerResult.ledger().closeTimeHuman().get()), latestLedgerResult.ledgerIndexSafe());
        }
        if (dt.isAfter(latestLedger.getPointInTime())) {
            logger.trace(String.format("%s is after last ledger -> take last ledger at %s", dt, latestLedger.getPointInTime()));
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

            bestMatch = get(estimatedFromLedgerIndex);

            logger.trace(String.format("Iteration %s: estOffset %s -> %s", iteration, estimatedOffset, bestMatch.getPointInTime()));
            iteration++;
        }

        return bestMatch;
    }

    private LedgerAtTime get(LedgerIndex index) throws JsonRpcClientErrorException {
        var ledger = cache.find(index);
        if (ledger != null) {
            return ledger;
        }

        var ledgerResult = xrplClient.ledger(LedgerRequestParams.builder().ledgerSpecifier(LedgerSpecifier.of(index)).build());
        return cache.add(DateTimeConvert.toLocal(ledgerResult.ledger().closeTimeHuman().get()), ledgerResult.ledgerIndexSafe());
    }

    private Duration getAverageLedgerDuration(LedgerAtTime ledger) throws JsonRpcClientErrorException {
        final UnsignedInteger referenceInterval = UnsignedInteger.valueOf(1000);
        var referenceLedgerIndex = ledger.getLedgerIndex().minus(referenceInterval);
        var referenceEarlier = get(referenceLedgerIndex);

        var durationSeconds = ChronoUnit.SECONDS.between(ledger.getPointInTime(), referenceEarlier.getPointInTime());
        var avgDurationPerLedger = durationSeconds / (double) referenceInterval.longValue();
        return Duration.ofMillis(Math.round(avgDurationPerLedger * 1000));
    }

    private boolean isLedgerAt(LedgerAtTime ledgerAtTime, LocalDateTime dt) {
        var closeTime = ledgerAtTime.getPointInTime();
        var diff = ChronoUnit.SECONDS.between(closeTime, dt);
        // accept ledger within a smaller timeframe.
        return 0 < diff && diff < Duration.ofSeconds(60).getSeconds();
    }
}
