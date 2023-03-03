package com.example.vkaudionotes.util

import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.vk.api.sdk.VKApiJSONResponseParser
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKHttpPostCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class VKFileUploadCommand(private val file:File? = null, private val url: String? = null):ApiCommand<String>() {

    val client = OkHttpClient()

    override fun onExecute(manager: VKApiManager): String {
        if(file!=null && url!=null) {
            return uploadFile(file, url = url, manager = manager)
        }
        return ""
    }
    private fun uploadFile(
        sourceFile: File,
        uploadedFileName: String? = null,
        url: String,
        manager: VKApiManager
    ): String {
        val newFile = File(sourceFile.parent,"newFile.txt")//upload something in progress
        newFile.writeText("asdasd")
        Log.d("VKUpload", "${newFile.toUri()}")
        Log.d("VKUpload", "source uri ${sourceFile.toUri()}")
        val fileUploadCall = VKHttpPostCall.Builder()
            .url(url)
            .args("file", newFile.toUri())
            .timeout(TimeUnit.MINUTES.toMillis(5))
            .retryCount(3)
            .build()

        Log.d("VKUpload", "before fileUploadInfo")
        Log.d("VKUpload", "${fileUploadCall.parts}")
        val fileUploadInfo = manager.execute(fileUploadCall, null, FileUploadInfoParser())
        return fileUploadInfo.file

        //        val mimeType = getMimeType(sourceFile);
//        if (mimeType == null) {
//            Log.e("file error", "Not able to get mime type")
//            return false
//        }
//        val fileName: String = if (uploadedFileName == null) sourceFile.name else uploadedFileName
//        try {
//            val requestBody: RequestBody =
//                MultipartBody.Builder().setType(MultipartBody.FORM)
//                    .addFormDataPart(
//                        "uploaded_file",
//                        fileName,
//                        sourceFile.asRequestBody(mimeType.toMediaTypeOrNull())
//                    )
//                    .build()
//
//            val request: Request = Request.Builder().url(url).post(requestBody).build()
//
//            val response: Response = client.newCall(request).execute()
//
//            if (response.isSuccessful) {
//                Log.d("File upload", "success")
//                return true
//            } else {
//                Log.e("File upload", "failed")
//                return false
//            }
//        } catch (ex: Exception) {
//            ex.printStackTrace()
//            Log.e("File upload", "failed")
//            return false
//        }
    }

    private class FileUploadInfoParser: VKApiJSONResponseParser<VKFileUploadInfo> {
        override fun parse(responseJson: JSONObject): VKFileUploadInfo{
            try {
                val joResponse = responseJson
                Log.d("parser","${joResponse.keys()} $joResponse")
                return VKFileUploadInfo(
                    file = joResponse.getString("file"),
                )
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }
    class VKFileUploadInfo(val file: String)

    // url = file path or whatever suitable URL you want.
    fun getMimeType(file: File): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(file.path)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }




}