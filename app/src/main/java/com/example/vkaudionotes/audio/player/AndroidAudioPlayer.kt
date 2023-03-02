package com.example.vkaudionotes.audio.player

import android.content.Context
import android.content.res.AssetManager
import android.media.MediaPlayer
import androidx.compose.runtime.MutableState
import androidx.core.net.toUri
import com.example.vkaudionotes.audio.visualizer.VisualizerComputer
import com.example.vkaudionotes.audio.visualizer.VisualizerData
import com.example.vkaudionotes.model.AudioFinishedException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class AndroidAudioPlayer(
    private val context: Context
): AudioPlayer {

    private var player: MediaPlayer? = null

    private val audioComputer = VisualizerComputer()

    override fun playFile(file: File): Flow<Int> {
        stop()
        MediaPlayer.create(context, file.toUri()).apply {
            player = this
            start()
        }
        return flow{
            val finalValue = player?.duration ?: 0
            while(player?.isPlaying == true){
                val position = player?.currentPosition
                if(position!=null)
                    emit(position)
                delay(100)
            }
            emit(finalValue)
            throw AudioFinishedException()
        }
    }
    fun play(assets: AssetManager, fileName: String, visualizerData: MutableState<VisualizerData>):Int {
        val afd = assets.openFd(fileName)
        player = MediaPlayer().apply {
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            setVolume(0.01f, 0.01f)
            prepare()
            start()
        }
        audioComputer.start(audioSessionId = player!!.audioSessionId, onData = { data ->
            visualizerData.value = data
        })
        return player?.currentPosition ?: 0
    }
    fun getFileDuration(file: File):Int{
        MediaPlayer.create(context, file.toUri()).apply {
            val duration = this.duration
            this.stop()
            this.release()
            return duration
        }
    }



    override fun stop() {
        player?.stop()
        player?.release()
        player = null
    }

    override fun pause() {
        player?.pause()
    }

    override fun resume():Flow<Int> {
        player?.start()
        return flow{
            val finalValue = player?.duration ?: 0
            while(player?.isPlaying == true){
                val position = player?.currentPosition
                if(position!=null)
                    emit(position)
                delay(100)
            }
            emit(finalValue)
            throw AudioFinishedException()
        }
    }
}