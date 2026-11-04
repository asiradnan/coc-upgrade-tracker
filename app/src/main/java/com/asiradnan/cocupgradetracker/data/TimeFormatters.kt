package com.asiradnan.cocupgradetracker.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

object TimeFormatters {
    private val shortTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    private val exactTimeFormatter = DateTimeFormatter.ofPattern("h:mm:ss a", Locale.getDefault())
    private val exactDateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
    private val fullDateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a", Locale.getDefault())

    fun formatDuration(seconds: Long): String {
        if (seconds <= 0L) return "Done"
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0 || days > 0) append("${hours}h ")
            if (minutes > 0 || hours > 0 || days > 0) append("${minutes}m ")
            append("${secs}s")
        }.trim()
    }

    fun formatCompactDuration(seconds: Long): String {
        if (seconds <= 0L) return "Done"
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return when {
            days > 0 -> if (hours > 0) "${days}d ${hours}h" else "${days}d ${minutes}m"
            hours > 0 -> if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
            minutes > 0 -> "${minutes}m ${secs}s"
            else -> "${secs}s"
        }
    }

    fun formatShortClockTime(
        finishEpochSeconds: Long,
        nowEpochSeconds: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val finishZdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(finishEpochSeconds), zoneId)
        val nowZdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nowEpochSeconds), zoneId)
        val finishDate: LocalDate = finishZdt.toLocalDate()
        val today: LocalDate = nowZdt.toLocalDate()

        val timePart = finishZdt.format(shortTimeFormatter).lowercase(Locale.getDefault())
        return when (finishDate) {
            today -> timePart
            today.plusDays(1) -> "$timePart (Tomorrow)"
            else -> "$timePart (${finishZdt.format(exactDateFormatter)})"
        }
    }

    fun formatExactFinishTime(
        finishEpochSeconds: Long,
        nowEpochSeconds: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val finishZdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(finishEpochSeconds), zoneId)
        val nowZdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(nowEpochSeconds), zoneId)
        val finishDate: LocalDate = finishZdt.toLocalDate()
        val today: LocalDate = nowZdt.toLocalDate()

        val timePart = finishZdt.format(exactTimeFormatter)
        return when (finishDate) {
            today -> "Today at $timePart"
            today.plusDays(1) -> "Tomorrow at $timePart (${finishZdt.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))})"
            else -> "${finishZdt.format(exactDateFormatter)} at $timePart"
        }
    }

    fun formatRelativeAgo(elapsedSeconds: Long): String {
        val safeSec = max(0L, elapsedSeconds)
        return when {
            safeSec < 10L -> "just now"
            safeSec < 60L -> "${safeSec}s ago"
            safeSec < 3600L -> "${safeSec / 60L}m ago"
            safeSec < 86400L -> "${safeSec / 3600L}h ago"
            else -> "${safeSec / 86400L}d ago"
        }
    }

    fun formatFullDateTime(
        epochSeconds: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), zoneId)
        return zdt.format(fullDateTimeFormatter)
    }
}

