package com.example.vkaudionotes.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.ResponseBody
import retrofit2.Call

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

private const val BASE_URL =
    "https://api.vk.com/method/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface VKApiService {
    @GET("account.get")
    suspend fun getAccountAppPermissions(
        @Header("Authorization") accessToken: String,
        @Query("user_id") user_id: String
    ): Call<ResponseBody>
//    @GET("a9ceeb6e-416d-4352-bde6-2203416576ac")
//    suspend fun getFlashSaleProducts(): FlashSaleProductsList
}

object VKApi {
    val retrofitService: VKApiService by lazy {
        retrofit.create(VKApiService::class.java)
    }
}