package com.example.vkaudionotes.ui.components.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

fun formatMilli(milli:Long):String{
    return String.format(
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(milli), // The change is in this line
        TimeUnit.MILLISECONDS.toSeconds(milli) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milli))
    )
}

fun formatMilliToDate(milli:Long):String{
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(milli), ZoneId.systemDefault())
    val today = LocalDateTime.now()
    return if(dateTime.year == today.year &&  dateTime.dayOfYear == today.dayOfYear){
        dateTime.format(
            DateTimeFormatter.ofPattern("Сегодня в HH:mm")
        )
    }else{
        dateTime.format(
            DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm")
        )
    }
}