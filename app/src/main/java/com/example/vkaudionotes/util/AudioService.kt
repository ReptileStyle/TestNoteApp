package com.example.vkaudionotes.util

import android.util.Log
import com.vk.api.sdk.VKApiJSONResponseParser
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject

class AudioService {
    inner class audioGetUploadUrl(): ApiCommand<String>(){
        fun audioGetUploadUrl(manager: VKApiManager):String{
            val call = VKMethodCall
                .Builder()
                .method("audio.getUploadServer")
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
            return audioGetUploadUrl(manager)
        }
    }
}