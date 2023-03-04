package com.example.vkaudionotes.ui.main.components

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.vkaudionotes.R
import com.example.vkaudionotes.model.AudioNote
import com.example.vkaudionotes.ui.components.util.formatMilli
import com.vanpra.composematerialdialogs.rememberMaterialDialogState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioNoteContainer(
    modifier: Modifier = Modifier,
    onPlayClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onDecodeClick: () -> Unit = {},
    onEditTitleConfirmClick: (String) -> Unit = {},
    onLoadFileToVK: (String) -> Unit,
    isPlaying: Boolean = false,
    isPaused: Boolean = false,
    currentPosition: Long = 0L,
    audioNote: AudioNote
) {
    val dialogState = rememberMaterialDialogState()
    ChooseActionDialog(
        dialogState = dialogState,
        note = audioNote,
        onDeleteClick = { onDeleteClick() },
        onConfirmClick = onEditTitleConfirmClick,
        onLoadFileToVK = onLoadFileToVK,
        onDecodeClick = onDecodeClick
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
                    append("${formatMilli(currentPosition)}/")
                append("${formatMilli(audioNote.length)}")
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

        Row(
            modifier = Modifier
                .height(2.dp)
                .constrainAs(progressBar) {
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                },
        ) {
            if (isPlaying) {
                val progress = currentPosition.toFloat() / audioNote.length.toFloat()
                Log.d("asd","$progress")
                Surface(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(progress.coerceAtLeast(0.000001f)),
                    color = Color(0xff2189ec)
                ) {
                    Row(Modifier.fillMaxSize()){}
                }
                Surface(
                    modifier = Modifier
                        .height(2.dp)
                        .weight((1 - progress).coerceAtLeast(0.000001f)),
                    color = Color.Transparent
                ) {
                    Row(Modifier.fillMaxSize()){}
                }
            }
        }

    }
}


