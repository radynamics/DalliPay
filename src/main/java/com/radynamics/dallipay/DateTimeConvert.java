package com.radynamics.dallipay;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DateTimeConvert {
    public static ZonedDateTime toUserTimeZone(ZonedDateTime dt) {
        return dt.withZoneSameInstant(ZoneId.systemDefault());
    }
}
