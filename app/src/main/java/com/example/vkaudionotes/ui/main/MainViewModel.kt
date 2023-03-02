package com.example.vkaudionotes.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.media.audiofx.Visualizer
import android.os.Build
import android.os.FileObserver
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkaudionotes.audio.player.AndroidAudioPlayer
import com.example.vkaudionotes.audio.recorder.AndroidAudioRecorder
import com.example.vkaudionotes.audio.visualizer.VisualizerData
import com.example.vkaudionotes.model.AudioFinishedException
import com.example.vkaudionotes.model.AudioNote
import com.example.vkaudionotes.ui.components.util.formatMilli
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
        AndroidAudioRecorder(context.applicationContext,viewModelScope)
    }

    private val player by lazy {
        AndroidAudioPlayer(context.applicationContext)
    }

    val myAudioNotes = mutableStateListOf<AudioNote>()

    var currentPositionOfPlayingAudio by mutableStateOf(0)
    var playingAudioName by mutableStateOf<String?>(null)
    var isAudioPaused by mutableStateOf(false)

    var playerJob: Job? = null


    val fileObserver = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        object : FileObserver(context.dataDir.path, ALL_EVENTS){
            override fun onEvent(event: Int, path: String?) {
                when(event){
                    CREATE -> {
                        viewModelScope.launch {
                            while (isRecording.last() == true){
                                Log.d("MainVM", "delay")
                                delay(500)
                            }
                            if (path != null) {
                                Log.d("MainVM", path)
                                myAudioNotes.add(
                                    processAudioFileByPath(path)
                                )
                            }
                        }
                    }
                    DELETE -> {
                        if(path!=null){
                            Log.d("MainVM",path)
                            myAudioNotes.removeIf { it.title==path }
                        }
                    }
                }
            }
        }
    } else {
        object : FileObserver(context.dataDir, ALL_EVENTS){
            override fun onEvent(event: Int, path: String?) {
                when(event){
                    CREATE -> {
                        viewModelScope.launch {
                            while(isRecording.last()==true) delay(500)
                            if(path!=null) {
                                Log.d("MainVM",path)
                                myAudioNotes.add(
                                    processAudioFileByPath(path)
                                )
                            }
                        }
                    }
                    DELETE -> {

                        if(path!=null){
                            Log.d("MainVM",path)
                            myAudioNotes.removeIf { it.title==path }
                        }

                    }
                }
            }
        }
    }

    private fun processAudioFileByPath(path:String):AudioNote{
        return AudioNote(
            length = formatMilli(player.getFileDuration(File(context.dataDir.path,path)).toLong()),
            title = path,
            date = Instant.ofEpochMilli(File(context.dataDir.path, path).lastModified())
                .atZone(
                    ZoneId.systemDefault()
                ).toLocalDateTime()
        )
    }

    init {
        //get names
        val myAudioFileNames = context.dataDir.listFiles()?.map { file ->
            file.name
        }?.filter { it.endsWith("mp3") } ?: listOf()
        //get durations
        Log.d("MainVM", "init - $myAudioFileNames")

        myAudioFileNames.forEach {

            Log.d("MainVM",it)
            myAudioNotes.add(
                processAudioFileByPath(it)
            )

        }
        fileObserver.startWatching()
    }

    val isRecording = recorder.isActiveFlow.receiveAsFlow()

    fun recordAudio() {
        Log.d("MainVM", "start recording")
        File(context.dataDir, "${myAudioNotes.size + 1}.mp3").also {
            recorder.start(it)
        }
    }

    fun stopRecording(){
        Log.d("MainVM", "stop recording")
        recorder.stop()
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