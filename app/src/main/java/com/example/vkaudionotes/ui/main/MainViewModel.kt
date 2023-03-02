package com.example.vkaudionotes.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.media.audiofx.Visualizer
import android.os.Build
import android.os.FileObserver
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkaudionotes.audio.player.AndroidAudioPlayer
import com.example.vkaudionotes.audio.recorder.AndroidAudioRecorder
import com.example.vkaudionotes.audio.visualizer.VisualizerData
import com.example.vkaudionotes.model.AudioFinishedException
import com.example.vkaudionotes.model.AudioNote
import com.example.vkaudionotes.repository.Repository
import com.example.vkaudionotes.ui.components.util.formatMilli
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext
    val context: Context,
    private val repository: Repository
) : ViewModel() {
    private val recorder by lazy {
        AndroidAudioRecorder(context.applicationContext,viewModelScope)
    }

    private val player by lazy {
        AndroidAudioPlayer(context.applicationContext)
    }

    var currentPositionOfPlayingAudio by mutableStateOf(0)
    var playingAudioNote by mutableStateOf<AudioNote?>(null)
    var isAudioPaused by mutableStateOf(false)

    var playerJob: Job? = null

    init {
        getNotes()
    }

    val isRecording = recorder.isActiveFlow.receiveAsFlow()

    var newNote: AudioNote? = null

    fun recordAudio() {
        Log.d("MainVM", "start recording")
        newNote = AudioNote(
            title = generateTitle()
        )
        File(context.dataDir, newNote!!.title).also {
            recorder.start(it)
        }
    }

    private fun generateTitle():String{
        var title = "Новая запись.mp3"
        var i = 1
        while (File(context.dataDir.path,title).exists()){
            title = "Новая запись($i).mp3"
            i++
        }
        return title
    }
    fun stopRecording(){
        Log.d("MainVM", "stop recording")
        try {
            recorder.stop()
            val duration = formatMilli(player.getFileDuration(File(context.dataDir.path, newNote!!.title)).toLong())

            newNote = newNote!!.copy(length = duration, date = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm")),)
            insertNote(newNote!!)
            newNote = null
        }catch (e:Exception){
            Log.d("MainVM","${e.message}")
        }
    }

    fun changeTitleOfNote(note: AudioNote,title: String){
        File(context.dataDir.path,note.title).renameTo(File(context.dataDir.path,title))
        updateNote(note.copy(title = title))
    }

    fun playAudio(
        note: AudioNote
    ) {
        isAudioPaused = false
        if (note == playingAudioNote) {
            player.resume()
            playerJob?.cancel()
            playerJob = viewModelScope.launch {
                try {
                    player.resume().collect { position ->
                        currentPositionOfPlayingAudio = position
                    }
                } catch (e: AudioFinishedException) {
                    playingAudioNote = null
                }
            }
        } else {
            playingAudioNote = note
            playerJob?.cancel()
            playerJob = viewModelScope.launch {
                try {
                    player.playFile(File(context.dataDir.path, note.title)).collect { position ->
                        currentPositionOfPlayingAudio = position
                    }
                } catch (e: AudioFinishedException) {
                    playingAudioNote = null
                }
            }
        }
    }


    fun pauseAudio() {
        isAudioPaused = true
        playerJob?.cancel()
        player.pause()
    }

    override fun onCleared() {
        Log.d("MainVM", "onCleared")
        recorder.stop()
        super.onCleared()
    }



    lateinit var notes: Flow<List<AudioNote>>

    private fun getNotes() {
        notes = repository.allNotes
    }

    fun insertNote(note: AudioNote) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(note)
        }
    }

    fun updateNote(note: AudioNote) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note:AudioNote) {
        viewModelScope.launch(Dispatchers.IO) {
            File(context.dataDir.path,note.title).delete()
            repository.deleteNote(note)
        }
    }

    fun getNote(noteId: Int): Flow<AudioNote> = repository.getNote(noteId)
}