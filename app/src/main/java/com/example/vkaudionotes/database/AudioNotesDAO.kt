package com.example.vkaudionotes.database

import android.provider.ContactsContract
import androidx.room.*
import com.example.vkaudionotes.model.AudioNote
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioNotesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: AudioNote)

    @Update
    suspend fun updateNote(note: AudioNote)

    @Delete
    suspend fun deleteNote(note: AudioNote)

    @Query("SELECT * FROM notes_table WHERE id = :noteId")
    fun getNote(noteId: Int): Flow<AudioNote>

    @Query("SELECT * FROM notes_table ORDER BY id DESC")
    fun getAllNotes(): Flow<List<AudioNote>>
}