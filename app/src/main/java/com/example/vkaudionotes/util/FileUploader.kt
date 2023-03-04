package com.example.vkaudionotes.util

import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.vk.api.sdk.VKApiJSONResponseParser
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKHttpPostCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

//class VKFileUploadCommand(private val file:File? = null, private val url: String? = null):ApiCommand<String>() {
//    override fun onExecute(manager: VKApiManager): String {
//        if(file!=null && url!=null) {
//            return uploadFile(file, url = url, manager = manager)
//        }
//        return ""
//    }
//    private fun uploadFile(
//        sourceFile: File,
//        uploadedFileName: String? = null,
//        url: String,
//        manager: VKApiManager
//    ): String {
//        val newFile = File(sourceFile.parent,"newFile.txt")//upload something in progress
//        newFile.writeText("asdasd")
//        Log.d("VKUpload", "${newFile.toUri()}")
//        Log.d("VKUpload", "source uri ${sourceFile.toUri()}")
//        val fileUploadCall = VKHttpPostCall.Builder()
//            .url(url)
//            .args("file", sourceFile.toUri())
//            .timeout(TimeUnit.MINUTES.toMillis(5))
//            .retryCount(3)
//            .build()
//        Log.d("VKUpload", "before fileUploadInfo")
//        Log.d("VKUpload", "${fileUploadCall.parts}")
//        val fileUploadInfo = manager.execute(fileUploadCall, null, FileUploadInfoParser())
//        return fileUploadInfo.file
//    }
//
//    private class FileUploadInfoParser: VKApiJSONResponseParser<VKFileUploadInfo> {
//        override fun parse(responseJson: JSONObject): VKFileUploadInfo{
//            try {
//                val joResponse = responseJson
//                Log.d("parser","${joResponse.keys()} $joResponse")
//                return VKFileUploadInfo(
//                    file = joResponse.getString("file"),
//                )
//            } catch (ex: JSONException) {
//                throw VKApiIllegalResponseException(ex)
//            }
//        }
//    }
//    class VKFileUploadInfo(val file: String)
//}
//
//class VKAudioUploadCommand(private val file:File? = null, private val url: String? = null):ApiCommand<VKAudioUploadCommand.VKUploadAudioResponse>() {
//    override fun onExecute(manager: VKApiManager): VKUploadAudioResponse {
//        if(file!=null && url!=null) {
//            return uploadFile(file, url = url, manager = manager)
//        }
//        throw Exception("null passed")
//    }
//    private fun uploadFile(
//        sourceFile: File,
//        uploadedFileName: String? = null,
//        url: String,
//        manager: VKApiManager
//    ): VKUploadAudioResponse {
//        val newFile = File(sourceFile.parent,"newFile.txt")//upload something in progress
//        newFile.writeText("asdasd")
//        Log.d("VKUpload", "${newFile.toUri()}")
//        Log.d("VKUpload", "source uri ${sourceFile.toUri()}")
//        val fileUploadCall = VKHttpPostCall.Builder()
//            .url(url)
//            .args("file", sourceFile.toUri())
//            .timeout(TimeUnit.MINUTES.toMillis(5))
//            .retryCount(3)
//            .build()
//        Log.d("VKUpload", "before fileUploadInfo")
//        Log.d("VKUpload", "${fileUploadCall.parts}")
//        val fileUploadInfo = manager.execute(fileUploadCall, null, FileUploadInfoParser())
//        return fileUploadInfo
//    }
//
//    private class FileUploadInfoParser: VKApiJSONResponseParser<VKUploadAudioResponse> {
//        override fun parse(responseJson: JSONObject): VKUploadAudioResponse {
//            try {
//                val joResponse = responseJson
//                joResponse.keys().forEach {
//                    Log.d("Parser","$it")
//                }
//
//                return VKUploadAudioResponse(
//                    audio = joResponse.getString("audio"),
//                    hash = joResponse.getString("hash"),
//                    redirect= joResponse.getString("redirect"),
//                    server = joResponse.getInt("server"),
//                )
//            } catch (ex: JSONException) {
//                Log.d("Parser","${responseJson.getString("error_msg")}")
//                throw VKApiIllegalResponseException(ex)
//            }
//        }
//    }
//    data class VKUploadAudioResponse(
//        val audio: String,
//        val hash: String,
//        val redirect: String,
//        val server: Int
//    )
//
//}