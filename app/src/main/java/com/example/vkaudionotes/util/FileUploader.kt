package com.example.vkaudionotes.util

import android.app.Activity
import android.app.ProgressDialog
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class FileUploader() {

    val client = OkHttpClient()


    fun uploadFile(sourceFile: File, uploadedFileName: String? = null, url: String):Boolean {
        val mimeType = getMimeType(sourceFile);
        if (mimeType == null) {
            Log.e("file error", "Not able to get mime type")
            return false
        }
        val fileName: String = if (uploadedFileName == null) sourceFile.name else uploadedFileName
        try {
            val requestBody: RequestBody =
                MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "uploaded_file",
                        fileName,
                        sourceFile.asRequestBody(mimeType.toMediaTypeOrNull())
                    )
                    .build()

            val request: Request = Request.Builder().url(url).post(requestBody).build()

            val response: Response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Log.d("File upload", "success")
                return true
            } else {
                Log.e("File upload", "failed")
                return false
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.e("File upload", "failed")
            return false
        }
    }

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