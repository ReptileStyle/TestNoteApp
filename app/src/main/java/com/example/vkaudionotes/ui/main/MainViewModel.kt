package com.example.vkaudionotes.ui.main

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.vkaudionotes.audio.player.AndroidAudioPlayer
import com.example.vkaudionotes.audio.recorder.AndroidAudioRecorder
import com.example.vkaudionotes.model.AudioFinishedException
import com.example.vkaudionotes.model.AudioNote
import com.example.vkaudionotes.repository.Repository
import com.example.vkaudionotes.ui.components.util.formatMilli
import com.example.vkaudionotes.ui.components.util.generateTitle
import com.example.vkaudionotes.util.*
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.sdk.api.base.dto.BaseOkResponse
import com.vk.sdk.api.base.dto.BaseUploadServer
import com.vk.sdk.api.docs.DocsService
import com.vk.sdk.api.docs.dto.DocsGetMessagesUploadServerType
import com.vk.sdk.api.docs.dto.DocsSaveResponse

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.vk.sdk.api.fave.FaveService

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext
    val context: Context,
    private val repository: Repository
) : ViewModel(), LifecycleObserver {
    private val recorder by lazy {
        AndroidAudioRecorder(context.applicationContext, viewModelScope)
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

    val amplitudeFlow = recorder.amplitudeChannel.receiveAsFlow()
    fun recordAudio() {
        newNote = AudioNote(
            title = generateTitle(context.dataDir.path).substringBefore('.')
        )
        val fileName = newNote!!.name
        try {
            File(context.dataDir, fileName).also {
                recorder.start(it)
            }
        }catch (e:Exception){
            File(context.dataDir, fileName).delete()//delete file if problem with recording
        }
    }



    fun stopRecording() {
        try {
            recorder.stop()
            Log.d("asd",newNote!!.name)
            newNote = newNote!!.copy(
                length = player.getFileDuration(File(context.dataDir.path, newNote!!.name)).toLong(),
                date = System.currentTimeMillis()
            )
            insertNote(newNote!!)
            Log.d("asd","note inserted")
            Toast.makeText(context,"Stopped recording",Toast.LENGTH_SHORT).show()
            newNote = null
        } catch (e: Exception) {
            Log.e("MainVM", "${e.message}")
            Toast.makeText(context,"${e.message}",Toast.LENGTH_SHORT).show()
        }
    }

    fun changeTitleOfNote(note: AudioNote, title: String) {
        if(File(context.dataDir.path, "${title}.3gpp").exists()) throw Exception("this file exists")
        File(context.dataDir.path, note.name).renameTo(File(context.dataDir.path, "${title}.3gpp"))
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
                    player.playFile(File(context.dataDir.path, note.name)).collect { position ->
                        currentPositionOfPlayingAudio = position
                    }
                } catch (e: AudioFinishedException) {
                    playingAudioNote = null
                }
            }
        }
    }

    fun uploadFileToVK2(name: String) {
        viewModelScope.launch {
            val result = UploadUtility().saveToDocs(File(context.dataDir.path, name))
            if(!result) Toast.makeText(context,"Failed to upload file",Toast.LENGTH_SHORT).show()
            else Toast.makeText(context,"File successfully uploaded",Toast.LENGTH_SHORT).show()
        }
    }



    fun decodeAudioIntoText(name: String) {
       VK.execute(AsrService().AsrGetUploadUrl(),object : VKApiCallback<String>{
           override fun fail(error: Exception) {
               Log.d("VKAsr","failed to get upload url ${error.message}")
           }
           override fun success(result: String) {
               Log.d("VKAsr","success, link = ${result}")
//               VK.execute(AsrService().AsrUploadFile(url = result, file = File(context.dataDir.path,title)),
//                   object : VKApiCallback<VKAsrUploadFileInfo>{
//                       override fun fail(error: Exception) {
//                           Log.d("VKAsr","failed to upload file: ${error.message}")
//                       }
//                       override fun success(result: VKAsrUploadFileInfo) {
//                           Log.d("VKAsr","successfully uploaded, result = ${result}")
//                       }
//                   }
//               )
               viewModelScope.launch {
                   try {
                       val response = AsrService().uploadFile(File(context.dataDir.path, name), uploadUrl = result)
                   }catch(e:Exception) {
                       Toast.makeText(context,"Failed to recognize speech",Toast.LENGTH_SHORT).show()
                   }
               }
             //  UploadUtility().uploadFile(File(context.dataDir.path,title), uploadedFileName = null,url = result)
           }
       })
    }


    fun pauseAudio() {
        isAudioPaused = true
        playerJob?.cancel()
        player.pause()
    }

    override fun onCleared() {
        Log.d("MainVM", "onCleared")
//        try {
//            File(context.dataDir.path,newNote!!.name).delete()
//        }catch (e:Exception){
//            Log.d("MainVM", "no file to delete")
//        }
        super.onCleared()
    }



    lateinit var notes: Flow<List<AudioNote>>

    private fun getNotes() {
        notes = repository.allNotes
    }

    private fun insertNote(note: AudioNote) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(note)
        }
    }

    private fun updateNote(note: AudioNote) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: AudioNote) {
        viewModelScope.launch(Dispatchers.IO) {
            File(context.dataDir.path, note.name).delete()
            repository.deleteNote(note)
        }
    }

    fun getNote(noteId: Int): Flow<AudioNote> = repository.getNote(noteId)

}