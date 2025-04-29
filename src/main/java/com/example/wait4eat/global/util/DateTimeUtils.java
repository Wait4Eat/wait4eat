package com.example.wait4eat.global.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateTimeUtils {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * OffsetDateTime을 KST(LocalDateTime)로 변환
     */
    public static LocalDateTime toKstLocalDateTime(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) return null;
        return offsetDateTime.atZoneSameInstant(KST).toLocalDateTime();
    }
}
