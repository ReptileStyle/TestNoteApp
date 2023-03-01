package com.example.vkaudionotes.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.media.audiofx.Visualizer
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkaudionotes.audio.player.AndroidAudioPlayer
import com.example.vkaudionotes.audio.recorder.AndroidAudioRecorder
import com.example.vkaudionotes.audio.visualizer.VisualizerData
import com.example.vkaudionotes.model.AudioFinishedException
import com.example.vkaudionotes.model.AudioNote
import com.example.vkaudionotes.ui.components.util.formatMilli
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

class MainViewModel(
    val context: Context
) : ViewModel() {

    //check some info about audioSessionId


    private val recorder by lazy {
        AndroidAudioRecorder(context.applicationContext)
    }

    private val player by lazy {
        AndroidAudioPlayer(context.applicationContext)
    }

    val myAudioNotes = mutableStateListOf<AudioNote>()

    var currentPositionOfPlayingAudio by mutableStateOf(0)
    var playingAudioName by mutableStateOf<String?>(null)
    var isAudioPaused by mutableStateOf(false)

    var playerJob: Job? = null

    init {
        //get names
        val myAudioFileNames = context.dataDir.listFiles()?.map { file ->
            file.name
        }?.filter { it.endsWith("mp3") } ?: listOf()
        //get durations
        Log.d("MainVM", "init - $myAudioFileNames")

        myAudioFileNames.forEach {

            val duration = player.getFileDuration(File(context.dataDir.path, it))
            myAudioNotes.add(
                AudioNote(
                    length = formatMilli(duration.toLong()),
                    title = it,
                    date = Instant.ofEpochMilli(File(context.dataDir.path, it).lastModified())
                        .atZone(
                            ZoneId.systemDefault()
                        ).toLocalDateTime()
                )
            )


        }
    }

    val isRecording = recorder.isActiveFlow.receiveAsFlow()

    fun recordAudio(isRecording: Boolean) {
        Log.d("MainVM", "recording")
        if (isRecording) {
            recorder.stop()
        } else {
            File(context.dataDir, "${myAudioNotes.size + 1}.mp3").also {
                recorder.start(it)
            }
        }
    }

    fun playAudio(
        title: String
    ) {
        isAudioPaused = false
        if (title == playingAudioName) {
            player.resume()
            playerJob?.cancel()
            playerJob = viewModelScope.launch {
                try {
                    player.resume().collect { position ->
                        currentPositionOfPlayingAudio = position
                    }
                } catch (e: AudioFinishedException) {
                    playingAudioName = null
                }
            }
        } else {
            playingAudioName = title
            playerJob?.cancel()
            playerJob = viewModelScope.launch {
                try {
                    player.playFile(File(context.dataDir.path, title)).collect { position ->
                        currentPositionOfPlayingAudio = position
                    }
                } catch (e: AudioFinishedException) {
                    playingAudioName = null
                }

            }
        }
    }

    fun deleteAudio(title: String) {
        File(context.dataDir.path, title).delete()
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
}