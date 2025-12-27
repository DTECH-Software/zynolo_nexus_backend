package com.zynolo_nexus.auth_service.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DateTimeUtil {

    private DateTimeUtil() {
    }

    public static Instant nowUtc() {
        return Instant.now();
    }

    public static ZonedDateTime toUtcZonedDateTime(Instant instant) {
        return instant == null ? null : instant.atZone(ZoneId.of("UTC"));
    }
}
