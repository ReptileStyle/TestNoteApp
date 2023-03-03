package com.example.vkaudionotes.util

import android.util.Log
import androidx.core.net.toUri
import com.vk.api.sdk.VKApiJSONResponseParser
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKHttpPostCall
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import com.vk.api.sdk.internal.HttpMultipartEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit


class AsrService() {
    inner class AsrGetUploadUrl():ApiCommand<String>(){
        fun asrGetUploadUrl(manager: VKApiManager):String{
            val call = VKMethodCall
                .Builder()
                .method("asr.getUploadUrl")
                .version("5.131")
                .build()
            try {
                val response = manager.execute(call,AsrGetUrlInfoParser())
                return response.url
            }catch (e:Exception){
                return ""
            }

        }

        private inner class AsrGetUrlInfoParser:
            VKApiJSONResponseParser<VKAsrGetUrlInfo> {
            override fun parse(responseJson: JSONObject): VKAsrGetUrlInfo {
                try {
                    val joResponse = responseJson.getJSONObject("response")
                    Log.d("parser","${joResponse.keys()} $joResponse")
                    return VKAsrGetUrlInfo(
                        url = joResponse.getString("upload_url"),
                    )
                } catch (ex: JSONException) {
                    throw VKApiIllegalResponseException(ex)
                }
            }
        }
        inner class VKAsrGetUrlInfo(val url: String)
        override fun onExecute(manager: VKApiManager): String {
            return asrGetUploadUrl(manager)
        }
    }
    inner class AsrUploadFile(private val url: String, private val file:File):ApiCommand<VKAsrUploadFileInfo>(){
        private fun asrUploadFile(manager: VKApiManager):VKAsrUploadFileInfo{
            Log.d("VKAsr","uri = ${file.toUri()}")
            val call = VKHttpPostCall.Builder()
                .url(url)
                .args("file",file.toUri())
              //  .multipart(true)
             //   .parts(mapOf("file" to HttpMultipartEntry.File(file.toUri())))
                .timeout(TimeUnit.MINUTES.toMillis(5))
                .retryCount(3)
                .build()
            Log.d("VKAsr","call = ${call.parts}")
            try {
                val response = manager.execute(call,null,AsrUploadFileInfoParser())
                return response
            }catch (e:Exception){
                throw Exception("failed to upload file")
            }

        }
        private inner class AsrUploadFileInfoParser:
            VKApiJSONResponseParser<VKAsrUploadFileInfo> {
            override fun parse(responseJson: JSONObject): VKAsrUploadFileInfo {
                try {
                    val sha = responseJson.getString("sha")
                    val secret = responseJson.getString("secret")
                    val hash = responseJson.getString("hash")
                    val server = responseJson.getString("server")
                    val userId = responseJson.getInt("user_id")
                    val requestId = responseJson.getString("request_id")
                    val appId = responseJson.getInt("app_id")
                    val meta = responseJson.getJSONObject("meta")
                    val duration = meta.getString("duration")
                    val size = meta.getString("size")
                    val type = meta.getString("type")
                    Log.d("parser","${responseJson.keys()}")
                    return VKAsrUploadFileInfo(app_id = appId,hash=hash,meta = Meta(
                        duration = duration,
                        size = size,
                        type = type
                    ),
                        secret=secret,
                        sha = sha,
                        server = server,
                        user_id = userId,
                        request_id = requestId
                    )
                } catch (ex: JSONException) {
                    val errorMessage = responseJson.getString("error_msg")
                    val errorCode = responseJson.getInt("error_code")
                    Log.d("Parser","${ex.message} $errorCode : $errorMessage")
                    throw VKApiIllegalResponseException(ex)
                }
            }
        }
        override fun onExecute(manager: VKApiManager): VKAsrUploadFileInfo {
            return asrUploadFile(manager)
        }


    }
    suspend fun uploadFile(
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