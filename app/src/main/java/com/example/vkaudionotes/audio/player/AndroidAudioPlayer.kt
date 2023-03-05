package com.example.vkaudionotes.audio.player

import android.content.Context
import android.content.res.AssetManager
import android.media.MediaPlayer
import androidx.compose.runtime.MutableState
import androidx.core.net.toUri
import com.example.vkaudionotes.model.AudioFinishedException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class AndroidAudioPlayer(
    private val context: Context
): AudioPlayer {

    private var player: MediaPlayer? = null


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

    override fun getFileDuration(file: File):Int{
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