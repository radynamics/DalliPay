package com.radynamics.dallipay.cryptoledger.xrpl.api;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface LedgerAtTimeProvider {
    Optional<LedgerAtTime> estimatedDaysAgo(long sinceDaysAgo) throws LedgerAtTimeException;

    LedgerAtTime estimatedAgoFallback(long sinceDaysAgo) throws LedgerAtTimeException;

    Optional<LedgerAtTime> at(ZonedDateTime dt) throws LedgerAtTimeException;
}
