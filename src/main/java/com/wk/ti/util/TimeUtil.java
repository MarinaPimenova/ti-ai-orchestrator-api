package com.wk.ti.util;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("unused")
@Slf4j
public class TimeUtil {

    private TimeUtil() {}

    private static final ZoneId UTC_TIMEZONE = ZoneId.of("UTC");

    public static String getDateAlias(Instant timestamp, ZoneId zoneIdTo) {
        ZonedDateTime userZonedDateTime = timestamp.atZone(UTC_TIMEZONE).withZoneSameInstant(zoneIdTo);

        ZonedDateTime nowZoned = ZonedDateTime.now(zoneIdTo);
        ZonedDateTime todayStartZoned = nowZoned.toLocalDate().atStartOfDay(zoneIdTo);
        ZonedDateTime startDayWeekAgoZoned = todayStartZoned.minusDays(7);
        ZonedDateTime startDay30DaysAgoZoned = todayStartZoned.minusDays(30);

        if (userZonedDateTime.isAfter(todayStartZoned)) { return "Today"; }
        else if (userZonedDateTime.isAfter(startDayWeekAgoZoned)) { return "Previous 7 days"; }
        else if (userZonedDateTime.isAfter(startDay30DaysAgoZoned)) { return "Previous 30 days"; }
        return "Earlier";
    }

    public static String getUserDate(Instant timestamp, ZoneId zoneIdTo) {
        LocalDate userDate = getCurrentUserDate(timestamp, zoneIdTo);
        return userDate.format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));
    }

    private static LocalDate getCurrentUserDate(Instant timestamp, ZoneId zoneIdTo) {
        return timestamp.atZone(UTC_TIMEZONE).withZoneSameInstant(zoneIdTo).toLocalDate();
    }

    public static Instant getStartChatHistoryDate(ZoneId userZoneId, int daysBack) {

        LocalDate dateInUserTimezone = Instant.now().atZone(UTC_TIMEZONE)
                .withZoneSameInstant(userZoneId).toLocalDate();
        ZonedDateTime startOfDayInUserTimezone = dateInUserTimezone.atStartOfDay(userZoneId);
        ZonedDateTime startDayOfHistoryInUserTimezone = startOfDayInUserTimezone.minusDays(daysBack);
        ZonedDateTime startTimestampOfHistoryInUtc = startDayOfHistoryInUserTimezone
                .withZoneSameInstant(UTC_TIMEZONE);

        return startTimestampOfHistoryInUtc.toInstant();
    }
}