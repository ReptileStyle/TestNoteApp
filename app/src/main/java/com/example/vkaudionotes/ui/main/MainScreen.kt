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


val testList = listOf(AudioNote(title = "audio.mp3"), AudioNote(), AudioNote())


@Preview
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var hasRecordAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val isRecording by viewModel.isRecording.collectAsState(initial = false)

    val notes by viewModel.notes.collectAsState(initial = listOf())



    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    hasRecordAudioPermission = isGranted
                }
            )
            // val visualizerData = remember { mutableStateOf(VisualizerData()) }
            FloatingActionButton(
                onClick = {
                    if (hasRecordAudioPermission) {
                        if (isRecording) {
                            viewModel.stopRecording()
                        } else
                            viewModel.recordAudio()
                    } else
                        launcher.launch(Manifest.permission.RECORD_AUDIO)
                },
                backgroundColor = Color(0xff2189ec),
                contentColor = Color.White
            ) {
                Icon(
                    modifier = Modifier.size(35.dp),
                    imageVector = ImageVector.vectorResource(id = if (isRecording) R.drawable.mic_off else R.drawable.mic),
                    contentDescription = "Record"
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
                        isPlaying = isPlaying,
                        isPaused = viewModel.isAudioPaused,
                        currentPosition = if (isPlaying) formatMilli(viewModel.currentPositionOfPlayingAudio.toLong()) else "",
                        onPauseClick = { viewModel.pauseAudio() },
                        onEditTitleConfirmClick = { title ->
                            viewModel.changeTitleOfNote(
                                item,
                                title
                            )
                        },
                        onLoadFileToVK = {viewModel.uploadFileToVK(it)}
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioNoteContainer(
    modifier: Modifier = Modifier,
    onPlayClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onEditTitleConfirmClick: (String) -> Unit = {},
    onLoadFileToVK: (String) -> Unit,
    isPlaying: Boolean = false,
    isPaused: Boolean = false,
    currentPosition: String = "2:18",
    audioNote: AudioNote
) {
    val dialogState = rememberMaterialDialogState()
//    DeleteAudioDialog(dialogState = dialogState, title = audioNote.title) {
//        onDeleteClick()
//    }
    ChooseActionDialog(
        dialogState = dialogState,
        note = audioNote,
        onDeleteClick = { onDeleteClick() },
        onConfirmClick = onEditTitleConfirmClick,
        onLoadFileToVK = onLoadFileToVK
    )
    ConstraintLayout(modifier.combinedClickable(
        onClick = {},
        onLongClick = { dialogState.show() }
    )) {
        val (title, date, time, button, progressBar) = createRefs()
        Text(
            text = audioNote.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top, 4.dp)
                start.linkTo(parent.start, 8.dp)
            },
            color = Color.Black,
            maxLines = 1
        )
        Text(
            text = audioNote.date, //audioNote.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm")),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.constrainAs(date) {
                top.linkTo(title.bottom, 4.dp)
                start.linkTo(parent.start, 8.dp)
            },
            color = Color(0xff606060),
            maxLines = 1
        )
        Text(
            text = buildAnnotatedString {
                if (isPlaying)
                    append("$currentPosition/")
                append("${audioNote.length}")
            },//audioNote.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm")),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.constrainAs(time) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                end.linkTo(button.start, 8.dp)
            },
            color = Color(0xff606060),
            maxLines = 1
        )
        Surface(
            modifier = Modifier
                .size(35.dp)
                .constrainAs(button) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, 8.dp)
                },
            color = if (isPlaying && !isPaused) Color(0xff6d7986) else Color(0xff2189ec),
            shape = CircleShape
        ) {
            if (isPlaying && !isPaused) {
                IconButton(
                    onClick = { onPauseClick() },
                    modifier = Modifier
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.pause),
                        contentDescription = "play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
            } else {
                IconButton(
                    onClick = { onPlayClick() },
                    modifier = Modifier
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}


@Composable
fun DeleteAudioDialog(
    dialogState: MaterialDialogState,
    title: String,
    onConfirmClick: () -> Unit
) {
    MaterialDialog(
        dialogState = dialogState,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
        buttons = {
            positiveButton(text = "Ок", onClick = onConfirmClick)
            negativeButton(text = "Отмена")
        }
    ) {
        Text(
            text = "Удалить аудиозапись $title?",
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ChooseActionDialog(
    dialogState: MaterialDialogState,
    note: AudioNote,
    onDeleteClick: () -> Unit,
    onConfirmClick: (String) -> Unit,
    onLoadFileToVK: (String)->Unit
){
    val editDialogState = rememberMaterialDialogState()
    val context = LocalContext.current
    val VKAuthLauncher = rememberLauncherForActivityResult(contract = VK.getVKAuthActivityResultContract()){result->
        when (result) {
            is VKAuthenticationResult.Success -> {
                Toast.makeText(context,"Successfully logged in",Toast.LENGTH_LONG).show()
                VK.saveAccessToken(accessToken = result.token.accessToken, secret = result.token.secret, userId = result.token.userId, context = context)
                Log.d("VKAuth","${result.token.accessToken}")
            }
            is VKAuthenticationResult.Failed -> {
                Toast.makeText(context,"Failed to login: ${result.exception.message}",Toast.LENGTH_LONG).show()
            }
        }
    }

    EditNoteDialog(
        dialogState = editDialogState,
        note = note,
        onDeleteClick = onDeleteClick,
        onConfirmClick = onConfirmClick)

    MaterialDialog(
        dialogState = dialogState,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp),) {
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                editDialogState.show()
                dialogState.hide()
            }) {
                Text(text = "Редактировать")
            }
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                if(!VK.isLoggedIn()) {
                    VKAuthLauncher.launch(arrayListOf(VKScope.WALL, VKScope.PHOTOS, VKScope.DOCS))
                }else{
                    onLoadFileToVK(note.title)
                }
                dialogState.hide()
            }) {
                Text(text = "Загрузить в документы вк")
            }
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    dialogState.hide()
                }) {
                Text(text = "Назад")
            }
        }
    }
}

@Composable
fun EditNoteDialog(
    dialogState: MaterialDialogState,
    note: AudioNote,
    onDeleteClick: () -> Unit,
    onConfirmClick: (String) -> Unit
) {
    var title by remember {
        mutableStateOf(note.title)
    }

    val deleteDialogState = rememberMaterialDialogState()
    DeleteAudioDialog(dialogState = deleteDialogState, title = note.title) {
        onDeleteClick()
    }
    MaterialDialog(
        dialogState = dialogState,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
        buttons = {
            positiveButton(text = "Ок", onClick = { onConfirmClick(title) })
            negativeButton(text = "Удалить", onClick = { deleteDialogState.show() })
        }
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.padding(16.dp),
        )
    }
}
