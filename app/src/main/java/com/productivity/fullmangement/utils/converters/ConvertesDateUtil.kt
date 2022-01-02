package com.productivity.fullmangement.utils.converters

import android.text.format.DateFormat
import androidx.room.TypeConverter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

enum class EnumDateTimeFormats(val format: String){
    NORMAL_DATE("dd/MM/yyyy"),
    NORMAL_DATE_TIME("dd/MM/yyyy - HH:mm"),
    SHOW_DATE_TIME("dd-MM-yyyy\nHH:mm"),
    HOURS_TIME("HH"),
    MINUTES_TIME("MM"),
}

fun Long.getDateFormattedFromMills(format: String, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): String ?{
    return if (this > 0){
        val calender = Calendar.getInstance(timeZone, Locale.ENGLISH)
        calender.timeInMillis = this
        DateFormat.format(format, calender).toString()
    } else {
        ""
    }
}

fun String.getTimeInMillsFromDateFormatted(format: String, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Long{
    val formatter = SimpleDateFormat(format, Locale.ENGLISH)
    formatter.timeZone = timeZone
    return try {
        formatter.parse(this).time
    } catch (e: ParseException) {
        -1
    }
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}