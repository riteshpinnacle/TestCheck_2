package com.ssccgl.pinnacle.testcheck_2

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface ApiService {
    @POST("/index")
    suspend fun fetchData(@Body request: FetchDataRequest): List<IndexResponse>

    @POST("/save_next")
    suspend fun saveAnswer(@Body request: SaveAnswerRequest): SaveAnswerResponse

    @POST("/submit")
    suspend fun submit(@Body request: SubmitRequest): SubmitResponse

    @POST("/paperCodeDetails")
    suspend fun fetchPaperCodeDetails(@Body request: FetchDataRequest): PaperCodeDetailsResponse
}

object RetrofitInstance {
    private val client = OkHttpClient.Builder()
        .connectTimeout(300, TimeUnit.SECONDS) // Connection timeout
        .readTimeout(300, TimeUnit.SECONDS)    // Read timeout
        .writeTimeout(300, TimeUnit.SECONDS)   // Write timeout
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://onlineexam.ssccglpinnacle.com")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}