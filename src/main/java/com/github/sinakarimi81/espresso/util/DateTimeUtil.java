package com.github.sinakarimi81.espresso.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    public static String rfc1123DateFormat(Instant dateTime) {
        return DateTimeFormatter.RFC_1123_DATE_TIME
                .withZone(ZoneOffset.UTC)
                .format(dateTime);
    }

}
