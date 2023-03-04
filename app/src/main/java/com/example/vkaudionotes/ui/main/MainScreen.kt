package com.example.vkaudionotes.ui.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.room.util.convertByteToUUID
import com.example.vkaudionotes.R
import com.example.vkaudionotes.model.AudioNote
import com.example.vkaudionotes.ui.components.util.formatMilli
import com.example.vkaudionotes.ui.main.components.AudioNoteContainer
import com.example.vkaudionotes.ui.main.components.ChooseActionDialog
import com.example.vkaudionotes.ui.main.components.EditNoteDialog
import com.example.vkaudionotes.ui.main.components.FloatingMicrophoneButton
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKAuthException
import com.vk.sdk.api.docs.DocsService

import java.time.format.DateTimeFormatter


@Preview
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {

    val isRecording by viewModel.isRecording.collectAsState(initial = false)

    val notes by viewModel.notes.collectAsState(initial = listOf())

    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            FloatingMicrophoneButton(
                onStartRecording = { viewModel.recordAudio() },
                onStopRecording = {viewModel.stopRecording()},
                isRecording = isRecording
            )
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
            LazyColumn() {
                items(notes) { item ->
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
            }
        }
    }
}



