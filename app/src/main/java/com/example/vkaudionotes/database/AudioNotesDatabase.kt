package com.example.vkaudionotes.database

import android.provider.ContactsContract
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.vkaudionotes.model.AudioNote
import com.example.vkaudionotes.util.Converters

@Database(entities = [AudioNote::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AudioNotesDatabase : RoomDatabase() {

    abstract fun audioNotesDao(): AudioNotesDAO
}