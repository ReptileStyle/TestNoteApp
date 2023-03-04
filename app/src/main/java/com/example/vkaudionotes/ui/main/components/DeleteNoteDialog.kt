package com.example.vkaudionotes.ui.main.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState

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