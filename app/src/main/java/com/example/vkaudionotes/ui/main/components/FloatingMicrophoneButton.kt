package com.example.vkaudionotes.ui.main.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.vkaudionotes.R

@Composable
fun FloatingMicrophoneButton(
    onStartRecording:()->Unit,
    onStopRecording:()->Unit,
    isRecording:Boolean
){
    val context = LocalContext.current
    var hasRecordAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
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
                    onStopRecording()
                } else
                    onStartRecording()
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