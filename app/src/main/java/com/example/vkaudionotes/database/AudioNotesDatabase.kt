package com.example.vkaudionotes.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.vkaudionotes.model.AudioNote

@Database(entities = [AudioNote::class], version = 1, exportSchema = false)
abstract class AudioNotesDatabase : RoomDatabase() {
    abstract fun audioNotesDao(): AudioNotesDAO
}