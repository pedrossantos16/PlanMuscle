package com.pedro.planmuscle.data.local.converter

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

object DateUtils {
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun epochDayToDateString(epochDay: Long): String {
        val millis = epochDay * 86_400_000L
        return sdf.format(Date(millis))
    }

    fun dateStringToEpochDay(dateStr: String): Long {
        return try {
            val date = sdf.parse(dateStr)
            (date?.time ?: 0L) / 86_400_000L
        } catch (e: Exception) {
            0L
        }
    }

    fun todayEpochDay(): Long {
        val millis = System.currentTimeMillis()
        return millis / 86_400_000L
    }
}
