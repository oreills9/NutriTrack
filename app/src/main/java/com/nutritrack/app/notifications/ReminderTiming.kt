package com.nutritrack.app.notifications

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

internal fun nextSundayAt(time: LocalTime, now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
    val candidate = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).with(time)
    return if (candidate.isBefore(now)) candidate.plusWeeks(1) else candidate
}

internal fun nextTimeToday(time: LocalTime, now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
    val candidate = now.toLocalDate().atTime(time)
    return if (candidate.isBefore(now)) candidate.plusDays(1) else candidate
}
