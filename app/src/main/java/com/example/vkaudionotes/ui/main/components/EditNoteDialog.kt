package com.example.vkaudionotes.ui.main.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.vkaudionotes.model.AudioNote
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState

@Composable
fun EditNoteDialog(
    dialogState: MaterialDialogState,
    note: AudioNote,
    onDeleteClick: () -> Unit,
    onConfirmClick: (String) -> Unit
) {
    var isError by remember {
        mutableStateOf(false)
    }
    val deleteDialogState = rememberMaterialDialogState()
    DeleteAudioDialog(dialogState = deleteDialogState, title = note.title) {
        onDeleteClick()
    }

    var title by remember {
        mutableStateOf(note.title)
    }
    LaunchedEffect(key1 = note.title){//for some reason title is not resetting after adding new note
        title=note.title
    }
    MaterialDialog(
        dialogState = dialogState,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
        onCloseRequest = {
            isError=false
            dialogState.hide()
        },
        buttons = {
            positiveButton(text = "Ок", onClick = {
                try {
                    onConfirmClick(title)
                } catch (e: Exception) {
                    if(title==note.title){
                        dialogState.hide()
                    }
                    else{
                        isError = true
                    }
                }
            }, disableDismiss = true)
            negativeButton(text = "Удалить", onClick = { deleteDialogState.show() })
        }
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { value ->
                title = value
                isError = false
            },
            modifier = Modifier.padding(16.dp),
            isError = isError,
            label = {
                if (isError) {
                    Text(text = "Имя файла занято")
                }
            }
        )
    }
}
