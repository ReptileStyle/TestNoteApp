package com.example.vkaudionotes.ui.main.components

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.vkaudionotes.model.AudioNote
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope

@Composable
fun ChooseActionDialog(
    dialogState: MaterialDialogState,
    note: AudioNote,
    onDeleteClick: () -> Unit,
    onConfirmClick: (String) -> Unit,
    onDecodeClick: () -> Unit,
    onLoadFileToVK: (String) -> Unit
) {
    val editDialogState = rememberMaterialDialogState()
    val context = LocalContext.current
    val VKAuthLauncher =
        rememberLauncherForActivityResult(contract = VK.getVKAuthActivityResultContract()) { result ->
            when (result) {
                is VKAuthenticationResult.Success -> {
                    Toast.makeText(context, "Successfully logged in", Toast.LENGTH_LONG).show()
                    VK.saveAccessToken(
                        accessToken = result.token.accessToken,
                        secret = result.token.secret,
                        userId = result.token.userId,
                        context = context
                    )
                    //               VK.saveAccessToken(accessToken = result.token.accessToken, secret = result.token.secret, userId = result.token.userId, expiresInSec = result.token.expiresInSec, createdMs = result.token.createdMs)
                    Log.d("VKAuth", "${result.token.accessToken}")
                }
                is VKAuthenticationResult.Failed -> {
                    Toast.makeText(
                        context,
                        "Failed to login: ${result.exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    EditNoteDialog(
        dialogState = editDialogState,
        note = note,
        onDeleteClick = onDeleteClick,
        onConfirmClick = onConfirmClick
    )

    MaterialDialog(
        dialogState = dialogState,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
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
                    if (!VK.isLoggedIn()) {
                        VKAuthLauncher.launch(
                            arrayListOf(
                                VKScope.WALL,
                                VKScope.DOCS,
                                VKScope.AUDIO
                            )
                        )
                    } else {
                        onLoadFileToVK(note.name)
                    }
                    dialogState.hide()
                }) {
                Text(text = "Загрузить в документы вк")
            }
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onDecodeClick()
                    dialogState.hide()
                }) {
                Text(text = "Расшифровать")
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

