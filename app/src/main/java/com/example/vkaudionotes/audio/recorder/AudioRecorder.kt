package com.example.vkaudionotes.audio.recorder

import androidx.compose.runtime.MutableState
import com.example.vkaudionotes.audio.visualizer.VisualizerData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
    val isActiveFlow: Channel<Boolean>
}