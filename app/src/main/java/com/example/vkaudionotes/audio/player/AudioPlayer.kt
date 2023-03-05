package com.example.vkaudionotes.audio.player

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioPlayer {
    fun getFileDuration(file: File):Int
    fun playFile(file: File): Flow<Int>
    fun stop()
    fun pause()
    fun resume(): Flow<Int>
}