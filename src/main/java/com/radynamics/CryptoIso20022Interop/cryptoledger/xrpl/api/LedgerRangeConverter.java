package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api;

import com.google.common.primitives.UnsignedInteger;
import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.ledger.LedgerHeader;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class LedgerRangeConverter {
    private final Logger logger;
    private XrplClient xrplClient;

    public LedgerRangeConverter(XrplClient xrplClient) {
        this.xrplClient = xrplClient;
        this.logger = LogManager.getLogger();
    }

    public LedgerIndexRange convert(DateTimeRange period) throws JsonRpcClientErrorException {
        var ledgerAtStart = findLedger(period.getStart());
        var ledgerAtEnd = findLedger(period.getEnd());

        logger.trace(String.format("RESULT: %s - %s", ledgerAtStart.ledger().closeTimeHuman().get(), ledgerAtEnd.ledger().closeTimeHuman().get()));
        return LedgerIndexRange.of(ledgerAtStart.ledgerIndexSafe(), ledgerAtEnd.ledgerIndexSafe());
    }

    private LedgerResult findLedger(LocalDateTime dt) throws JsonRpcClientErrorException {
        logger.trace(String.format("Find ledger at %s", dt));
        var latestLedger = xrplClient.ledger(LedgerRequestParams.builder().ledgerSpecifier(LedgerSpecifier.CLOSED).build());
        var latestLedgerCloseTimeHuman = latestLedger.ledger().closeTimeHuman().get();
        if (dt.isAfter(latestLedgerCloseTimeHuman.toLocalDateTime())) {
            logger.trace(String.format("%s is after last ledger -> take last ledger at %s", dt, latestLedgerCloseTimeHuman.toLocalDateTime()));
            return latestLedger;
        }

        var avgDurationPerLedger = getAverageLedgerDuration(latestLedger.ledger());

        int iteration = 0;
        final int maxIterations = 20;
        var bestMatch = latestLedger;
        while (!isLedgerAt(bestMatch, dt) && iteration < maxIterations) {
            var fromDifference = Duration.ofMillis(ChronoUnit.MILLIS.between(dt, bestMatch.ledger().closeTimeHuman().get().toLocalDateTime()));
            var isTooEarly = fromDifference.isNegative();
            var estimatedOffsetSecods = fromDifference.toMillis() / (double) avgDurationPerLedger.toMillis();
            // ceil: if last found is slightly too early just take the next one.
            var estimatedOffset = UnsignedInteger.valueOf(Math.abs(Math.round(Math.ceil(estimatedOffsetSecods))));
            if (estimatedOffset.equals(UnsignedInteger.ZERO)) {
                break;
            }
            var estimatedFromLedgerIndex = isTooEarly
                    ? bestMatch.ledgerIndex().get().plus(estimatedOffset)
                    : bestMatch.ledgerIndex().get().minus(estimatedOffset);

            bestMatch = xrplClient.ledger(LedgerRequestParams.builder().ledgerSpecifier(LedgerSpecifier.of(estimatedFromLedgerIndex)).build());

            logger.trace(String.format("Iteration %s: estOffset %s -> %s", iteration, estimatedOffset, bestMatch.ledger().closeTimeHuman().get()));
            iteration++;
        }

        return bestMatch;
    }

    private Duration getAverageLedgerDuration(LedgerHeader ledger) throws JsonRpcClientErrorException {
        final UnsignedInteger referenceInterval = UnsignedInteger.valueOf(1000);
        var referenceLedgerIndex = ledger.ledgerIndex().minus(referenceInterval);
        var referenceEarlier = xrplClient.ledger(LedgerRequestParams.builder().ledgerSpecifier(LedgerSpecifier.of(referenceLedgerIndex)).build());

        var durationSeconds = ledger.closeTime().get().minus(referenceEarlier.ledger().closeTime().get());
        var avgDurationPerLedger = durationSeconds.longValue() / (double) referenceInterval.longValue();
        return Duration.ofMillis(Math.round(avgDurationPerLedger * 1000));
    }

    private boolean isLedgerAt(LedgerResult ledgerResult, LocalDateTime dt) {
        var closeTime = ledgerResult.ledger().closeTimeHuman().get().toLocalDateTime();
        var diff = ChronoUnit.SECONDS.between(closeTime, dt);
        // accept ledger within a smaller timeframe.
        return 0 < diff && diff < Duration.ofSeconds(60).getSeconds();
    }
}
