package com.tushu.adt.test;

import org.junit.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TestSth {

    @Test
    public void testDT() {
        LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneOffset.UTC);
        String day = dt.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        System.out.println(dt);
        System.out.printf("%s %02d\n", day, dt.getHour());
        System.out.printf("%04d%02d%02d\n", dt.getYear(), dt.getMonthValue(), dt.getDayOfMonth());
        System.out.println(dt.format(DateTimeFormatter.ofPattern("'pkg_'yyMMddHHmmss")));

//        ZoneOffset.getAvailableZoneIds().forEach(System.out::println);
        int hour = LocalTime.now(Clock.systemUTC()).getHour();
        System.out.printf("UTC: %d\n", hour);
        System.out.printf("UTC+8: %d\n", (hour + 8 + 24) % 24);
        System.out.printf("UTC-8: %d\n", (hour - 8 + 24) % 24);

        String zid = String.format("UTC%+d", 8);
        System.out.println(zid);
        System.out.println(LocalTime.now(ZoneId.of(zid)));

    }


}
