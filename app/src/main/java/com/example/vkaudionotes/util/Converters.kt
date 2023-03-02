package com.example.vkaudionotes.util

import androidx.room.TypeConverter
import java.util.*

class Converters {

    @TypeConverter
    fun fromTimeStamp(value: Long) = Date(value)

    @TypeConverter
    fun dateToTimeStamp(date: Date) = date.time
}