package com.example.vkaudionotes.ui.main

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkaudionotes.audio.player.AndroidAudioPlayer
import com.example.vkaudionotes.audio.recorder.AndroidAudioRecorder
import com.example.vkaudionotes.model.AudioFinishedException
import com.example.vkaudionotes.model.AudioNote
import com.example.vkaudionotes.repository.Repository
import com.example.vkaudionotes.ui.components.util.formatMilli
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
) : ViewModel() {
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
        val token =
            "vk1.a.afpEhJvfcWad-GQYBiNSw-2q8QECx2aBbmlx6-6axtP1G73ACCNCCpqm4Lwea_ps442tBHnJ6za3_3HNCZzynhdNOVZkrzmCXdMNpBmwjtBPAUArwULwXyDuu9WXNag6fHNUqXJJlo5h-gwCB998gmZfqbyEk7bpWQASdQa2hOnLlXxBk5zsQ1hkglaGRjeXZS643AhZ2t43UlvenfqY1w"
       // VK.logout()
//        viewModelScope.launch {
//            try {
//                val i = VKApi.retrofitService.getAccountAppPermissions(
//                    accessToken = "Bearer: $token",
//                    user_id = "${VK.getUserId().value}"
//                )
//                val a = i.execute()
//                if(a.isSuccessful){
//                    Log.d("MainVM","a=${a.body()}")
//                }
//                Log.d("MainVM","i=${i}")
//            }catch (e:Exception){
//                Log.d("MainVM","${e.message}")
//            }
//        }
        getNotes()
    }

    val isRecording = recorder.isActiveFlow.receiveAsFlow()

    var newNote: AudioNote? = null

    fun recordAudio() {
        Log.d("MainVM", "start recording")
        newNote = AudioNote(
            title = generateTitle().substringBefore('.')
        )
        File(context.dataDir, newNote!!.name).also {
            recorder.start(it)
        }
    }

    private fun generateTitle(): String {
        var title = "Новая запись.3gpp"
        var i = 1
        while (File(context.dataDir.path, title).exists()) {
            title = "Новая запись($i).3gpp"
            i++
        }
        return title
    }

    fun stopRecording() {
        Log.d("MainVM", "stop recording")
        try {
            recorder.stop()
            Log.d("asd",newNote!!.name)
            val duration = formatMilli(
                player.getFileDuration(File(context.dataDir.path, newNote!!.name)).toLong()
            )

            newNote = newNote!!.copy(
                length = duration,
                date = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm")
                ),
            )
            insertNote(newNote!!)
            newNote = null
        } catch (e: Exception) {
            Log.d("MainVM", "${e.message}")
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

    fun uploadFileToVK(name: String) {
        VK.execute(DocsService().docsGetMessagesUploadServer(type = DocsGetMessagesUploadServerType.AUDIO_MESSAGE), object : VKApiCallback<BaseUploadServer> {
            override fun fail(error: Exception) {
                Log.d("VK", "failed to get upload url ${error.message}")
                Log.d("VK", "failed to get upload url ${error.toString()}")
                Log.d("VK", "failed to get upload url ${error.localizedMessage}")
            }

            override fun success(result: BaseUploadServer) {
                Log.d("VK", "got upload url")
              //  FileUploader().uploadFile(File(context.dataDir.path, title), url = result.uploadUrl)
                VK.execute(
                    VKFileUploadCommand(
                        File(context.dataDir.path, name),
                        url = result.uploadUrl
                    ), object : VKApiCallback<String> {
                        override fun fail(error: Exception) {
                            Log.d("VKUpload","file upload failed")
                        }
                        override fun success(result: String) {
                            Log.d("VKUpload","file upload success, $result")
                            VK.execute(DocsService().docsSave(file = result, title = name),object:VKApiCallback<DocsSaveResponse>{
                                override fun fail(error: Exception) {
                                    Log.d("VKUpload","failed to save file")
                                }
                                override fun success(result: DocsSaveResponse) {
                                    Log.d("VKUpload","successfully saved file ${result.audioMessage?.linkOgg}")
                                    VK.execute(FaveService().faveAddLink(
                                        link = "${result.audioMessage?.linkOgg}"
                                    ),object : VKApiCallback<BaseOkResponse>{
                                        override fun fail(error: Exception) {
                                            Log.d("VKUpload","message error: ${error.message}")
                                        }

                                        override fun success(result: BaseOkResponse) {
                                            Log.d("VKUpload","link added to fave")
                                        }

                                    })
                                }
                            })
                        }
                    })
            }
        })
    }
    fun uploadFileToVK2(name: String) {
//        VK.execute(AudioService().audioGetUploadUrl(),object : VKApiCallback<String>{
//            override fun fail(error: Exception) {
//                Log.d("VKAudio","failed to get upload link")
//            }
//
//            override fun success(result: String) {
//                Log.d("VKAudio","success to get upload link: $result")
////                VK.execute(VKAudioUploadCommand(
////                    url = result,
////                    file =  File(context.dataDir.path, title),
////                ), object : VKApiCallback<VKAudioUploadCommand.VKUploadAudioResponse> {
////                    override fun fail(error: Exception) {
////                        Log.d("VKAudio","failed to upload audio to server: ${error.message}")
////                    }
////
////                    override fun success(result: VKAudioUploadCommand.VKUploadAudioResponse) {
////                        Log.d("VKAudio","success to upload audio to server")
////                    }
////
////                })
////                viewModelScope.launch {
////                    UploadUtility().saveToAudio(File(context.dataDir.path, title), result)
////                }
//            }
//        })
        viewModelScope.launch {
            UploadUtility().saveToDocs(File(context.dataDir.path, name))
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
                   val response = AsrService().uploadFile(File(context.dataDir.path, name), uploadUrl = result)
                   Log.d("VKAsr","$response")
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

    fun deleteNote(note: AudioNote) {
        viewModelScope.launch(Dispatchers.IO) {
            File(context.dataDir.path, note.title).delete()
            repository.deleteNote(note)
        }
    }

    fun getNote(noteId: Int): Flow<AudioNote> = repository.getNote(noteId)
}