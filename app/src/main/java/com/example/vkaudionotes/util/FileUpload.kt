package com.example.vkaudionotes.util

import android.app.Activity
import android.app.ProgressDialog
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap

import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.internal.ApiCommand
import com.vk.sdk.api.docs.DocsService
import com.vk.sdk.api.wall.dto.WallPostSourceType
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.http.Url
import java.io.File
import java.net.URL

class UploadUtility() {

    private val remoteScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun saveToDocs(file: File) = suspendCancellableCoroutine<Boolean> { cont ->
        remoteScope.launch {
            runCatching {
                val uploadUrl = VK.enqueue(DocsService().docsGetUploadServer()).uploadUrl
                val body = uploadFile(file, uploadUrl)!!
                val vkFile = Json.decodeFromString<VkFile>(body).file

                VK.enqueue(DocsService().docsSave(vkFile, file.name))
                cont.resumeWith(Result.success(true))
            }.onFailure {
                it.printStackTrace()
                cont.resumeWith(Result.success(false))
            }
        }
    }

    suspend fun saveToAudio(file: File, url:String) = suspendCancellableCoroutine<Boolean> { cont ->
        remoteScope.launch {
            runCatching {
                val body = uploadFile(file, url)!!
                val audio = Json.decodeFromString<VKUploadAudioResponse>(body)
                Log.d("loader", audio.toString())
            //    VK.enqueue(DocsService().docsSave(vkFile, file.name))
                cont.resumeWith(Result.success(true))
            }.onFailure {
                it.printStackTrace()
                cont.resumeWith(Result.success(false))
            }
        }
    }


    private suspend fun uploadFile(
        file: File,
        uploadUrl: String
    ): String? = withContext(Dispatchers.IO) {
        val formBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody())
            .build()

        val call = OkHttpClient().newCall(
            Request.Builder()
                .post(formBody)
                .url(uploadUrl)
                .build()
        )
        return@withContext call.execute().body?.string()
    }
}

private suspend fun <T> VK.enqueue(request: ApiCommand<T>) = suspendCancellableCoroutine<T> { cont ->
    execute(request, object : VKApiCallback<T> {
        override fun fail(error: Exception) {
            cont.resumeWithException(error)
        }

        override fun success(result: T) {
            cont.resume(result)
        }
    })

}

@Serializable
data class VkFile(val file: String)

@Serializable
data class VKUploadAudioResponse(
    val audio: String,
    val hash: String,
    val redirect: String,
    val server: Int
)