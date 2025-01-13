package com.example.todoreminder.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object HelperUtils {
    // Keep formatters for display purposes
    private val displayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")

    fun formatDateTime(timestamp: Long?): String {
        return if (timestamp == null) "No date set"
        else {
            try {
                val instant = Instant.ofEpochMilli(timestamp)
                val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                dateTime.format(displayFormatter)
            } catch (e: Exception) {
                "Invalid date"
            }
        }
    }

    fun toLocalDateTime(timestamp: Long): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
    }

    fun fromLocalDateTime(dateTime: LocalDateTime): Long {
        return dateTime.atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    // If you need to convert existing String dates to Long during migration
    fun convertStringDateToLong(dateString: String): Long? {
        return try {
            val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm")
            val dateTime = LocalDateTime.parse(dateString, formatter)
            dateTime.atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) {
            null
        }
    }
}