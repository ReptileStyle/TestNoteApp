package com.example.vkaudionotes.audio.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.lifecycle.MutableLiveData

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
) : AudioRecorder {
    private var recorder: MediaRecorder? = null

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    override val isActiveFlow = Channel<Boolean>()


    override val amplitudeChannel = Channel<Int>()


    private val timer = object : CountDownTimer(60000 * 60 - 1000, 1000) {
        //not allowed to record more, than 59:59
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
                    Log.d("recorder", "true sended")
                    isActiveFlow.send(true)
                }
            }
            scope.launch {
                while (true) {
                    delay(350)
                    try {
                        val amplitude = recorder?.maxAmplitude
                        if (amplitude != null)
                            amplitudeChannel.send(amplitude)
                        else
                            break
                    }catch (e:Exception){
                        //getMaxAmplitude called in an invalid state
                    }
                }
            }
        }
    }


    override fun stop() {
        timer.cancel()
        try {
            recorder?.stop()
            recorder?.reset()
        }catch (e:Exception){
            //stop failed, because never started?
        }
        recorder = null.also {
            scope.launch {
                Log.d("recorder", "false sended")
                isActiveFlow.send(false)
            }
        }
    }
}