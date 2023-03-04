package com.example.vkaudionotes.util

@kotlinx.serialization.Serializable
data class VKAsrUploadFileInfo(
    val app_id: Int,
    val hash: String,
    val meta: Meta,
    val request_id: String,
    val secret: String,
    val server: String,
    val sha: String,
    val user_id: Int
)
@kotlinx.serialization.Serializable
data class Meta(
    val duration: String,
    val size: String,
    val type: String
)