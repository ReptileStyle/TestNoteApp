package com.example.vkaudionotes.model

import java.time.LocalDateTime

data class AudioNote(
    val length:String = "0:00",
    val date: LocalDateTime = LocalDateTime.now(),
    val title: String = "Default title",
)