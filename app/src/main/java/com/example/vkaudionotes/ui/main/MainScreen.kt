package com.example.vkaudionotes.ui.main

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.example.vkaudionotes.model.AudioNote
import com.example.vkaudionotes.ui.components.visualizer.VoiceVisualizerDots
import com.example.vkaudionotes.ui.main.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    var isRecording by remember {
        mutableStateOf(false)
    }
    var isClickable by remember {
        mutableStateOf(true)
    }
    var notes by remember {
        mutableStateOf<List<AudioNote>>(listOf())
    }
    val lazyListState = rememberLazyListState()
    LaunchedEffect(key1 = true) {
        this.launch {
            viewModel.isRecording.collect {
                isRecording = it
                this.launch {
                    delay(500)//errors if click too fast
                    isClickable = true
                }
            }
        }
        this.launch {
            viewModel.notes.collect {
                Log.d("asd", "${it.size}")
                notes = it
                this.launch {
                    lazyListState.scrollToItem(0)
                }
            }
        }
    }
    //   val isRecording by viewModel.isRecording.collectAsState(initial = false)

    //val notes by viewModel.notes.collectAsState(initial = listOf())

    val amplitude by viewModel.amplitudeFlow.collectAsState(initial = 0)

    OnLifecycleEvent { owner, event ->
        // do stuff on event
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                if (isRecording)
                    viewModel.stopRecording()
            }//because onDestroy i cant properly save file or even delete it
            else -> { /* other stuff */
            }
        }
    }

    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isRecording) {
                    VoiceVisualizerDots(amplitude = amplitude, color = Color(0xff2189ec))
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingMicrophoneButton(
                    onStartRecording = {
                        if (isClickable) {
                            isClickable = false
                            viewModel.recordAudio()
                        }
                    },
                    onStopRecording = {
                        if (isClickable) {
                            isClickable = false
                            viewModel.stopRecording()
                        }
                    },
                    isRecording = isRecording
                )
            }

        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 8.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Ваши записи",
                fontSize = 30.sp,
                fontWeight = FontWeight.Medium,
            )
            LazyColumn(state = lazyListState) {
                items(notes, key = { it.id }) { item ->
                    val isPlaying = item == viewModel.playingAudioNote
                    AudioNoteContainer(
                        audioNote = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(65.dp),
                        onPlayClick = { viewModel.playAudio(item) },
                        onDeleteClick = { viewModel.deleteNote(item) },
                        onDecodeClick = { viewModel.decodeAudioIntoText(item.name) },
                        isPlaying = isPlaying,
                        isPaused = viewModel.isAudioPaused,
                        currentPosition = if (isPlaying) viewModel.currentPositionOfPlayingAudio.toLong() else 0L,
                        onPauseClick = { viewModel.pauseAudio() },
                        onEditTitleConfirmClick = { title ->
                            viewModel.changeTitleOfNote(
                                item,
                                title
                            )
                        },
                        onLoadFileToVK = { viewModel.uploadFileToVK2(it) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}



