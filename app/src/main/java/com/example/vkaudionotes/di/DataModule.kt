package com.example.vkaudionotes.di

import android.content.Context
import androidx.room.Room
import com.example.vkaudionotes.database.AudioNotesDAO
import com.example.vkaudionotes.database.AudioNotesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideAudioNotesDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AudioNotesDatabase::class.java, "audio_notes_database.db")
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun provideNoteDao(database: AudioNotesDatabase): AudioNotesDAO {
        return database.audioNotesDao()
    }
}