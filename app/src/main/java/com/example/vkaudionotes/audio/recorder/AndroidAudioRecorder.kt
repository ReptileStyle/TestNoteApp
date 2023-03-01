package com.example.vkaudionotes.audio.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.lifecycle.MutableLiveData
import com.example.vkaudionotes.audio.visualizer.VisualizerComputer
import com.example.vkaudionotes.audio.visualizer.VisualizerData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext

class AndroidAudioRecorder(
private val context: Context
): AudioRecorder {
    private var recorder: MediaRecorder? = null

    private fun createRecorder(): MediaRecorder {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    override val isActiveFlow = Channel<Boolean>()

    private val audioComputer = VisualizerComputer()




    override fun start(outputFile: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)
            prepare()
            start()

            recorder = this.also {
                CoroutineScope(Dispatchers.Default).launch {
                    isActiveFlow.send(recorder!=null)
                }
            }

        }
    }




    override fun stop() {
        recorder?.stop()
        recorder?.reset()
        recorder = null.also {
            CoroutineScope(Dispatchers.Default).launch {
                isActiveFlow.send(recorder!=null)
            }
        }
    }
}