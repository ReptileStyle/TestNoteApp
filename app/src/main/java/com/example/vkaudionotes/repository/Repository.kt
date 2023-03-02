package com.example.vkaudionotes.repository

import android.provider.ContactsContract
import com.example.vkaudionotes.database.AudioNotesDAO
import com.example.vkaudionotes.model.AudioNote
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(private val audioNotesDAO: AudioNotesDAO) {

    val allNotes: Flow<List<AudioNote>> = audioNotesDAO.getAllNotes()

    suspend fun insertNote(note: AudioNote) {
        audioNotesDAO.insertNote(note)
    }

    suspend fun updateNote(note: AudioNote) {
        audioNotesDAO.updateNote(note)
    }

    suspend fun deleteNote(note: AudioNote) {
        audioNotesDAO.deleteNote(note)
    }

    fun getNote(noteId: Int): Flow<AudioNote> = audioNotesDAO.getNote(noteId)
}