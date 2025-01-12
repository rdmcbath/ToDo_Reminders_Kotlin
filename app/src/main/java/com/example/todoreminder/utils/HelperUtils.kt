package com.example.todoreminder.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object HelperUtils {
    private val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm")
    private val displayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")

    fun formatDateTime(dateString: String?): String {
        return if (dateString == null) "No date set"
        else {
            try {
                val dateTime = LocalDateTime.parse(dateString, formatter)
                dateTime.format(displayFormatter)
            } catch (e: Exception) {
                "Invalid date"
            }
        }
    }

    fun toLocalDateTime(dateString: String): LocalDateTime {
        return LocalDateTime.parse(dateString, formatter)
    }

    fun fromLocalDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(formatter)
    }
}