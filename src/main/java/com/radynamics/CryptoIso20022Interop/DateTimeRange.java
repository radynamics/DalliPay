package com.radynamics.CryptoIso20022Interop;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTimeRange {
    private ZonedDateTime start;
    private ZonedDateTime end;

    private DateTimeRange(ZonedDateTime start, ZonedDateTime end) {
        this.start = start;
        this.end = end;
    }

    public static DateTimeRange of(LocalDateTime start, LocalDateTime end) {
        return new DateTimeRange(start.atZone(ZoneId.systemDefault()), end.atZone(ZoneId.systemDefault()));
    }

    public static DateTimeRange of(ZonedDateTime start, ZonedDateTime end) {
        return new DateTimeRange(start, end);
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public ZonedDateTime getEnd() {
        return end;
    }

    public boolean isBetween(ZonedDateTime pointInTime) {
        return start.isBefore(pointInTime) && pointInTime.isBefore(end);
    }
}
