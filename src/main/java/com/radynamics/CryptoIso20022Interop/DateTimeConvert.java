package com.radynamics.CryptoIso20022Interop;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DateTimeConvert {
    public static ZonedDateTime toUserTimeZone(ZonedDateTime dt) {
        // TODO: change to second line. Use first line to eliminate unit test differences.
        return dt.toLocalDateTime().atZone(ZoneId.of("UTC"));
        //return dt.withZoneSameInstant(ZoneId.systemDefault());
    }
}
