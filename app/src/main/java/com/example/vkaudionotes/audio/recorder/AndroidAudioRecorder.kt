package com.example.vkaudionotes.audio.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
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
private val context: Context,
private val scope: CoroutineScope
): AudioRecorder {
    private var recorder: MediaRecorder? = null

    private fun createRecorder(): MediaRecorder {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    override val isActiveFlow = Channel<Boolean>()

    private val audioComputer = VisualizerComputer()

    private val timer = object : CountDownTimer(60000*60-2000, 1000){//not allowed to record more, than 59:59
        override fun onTick(millisUntilFinished: Long) {

        }
        override fun onFinish() {
            stop()
        }
    }


    override fun start(outputFile: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)
            prepare()
            start()
            timer.start()

            recorder = this.also {
                scope.launch {
                    Log.d("recorder","true sended")
                    isActiveFlow.send(true)
                }
            }

        }
    }




    override fun stop() {
        timer.cancel()
        recorder?.stop()
        recorder?.reset()
        recorder = null.also {
            scope.launch {
                Log.d("recorder","false sended")
                isActiveFlow.send(false)
            }
        }
    }
}