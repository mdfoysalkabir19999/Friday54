package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini REST API Models with Moshi adapter generation ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<MoshiContent>,
    @Json(name = "systemInstruction") val systemInstruction: MoshiContent? = null
)

@JsonClass(generateAdapter = true)
data class MoshiContent(
    @Json(name = "role") val role: String? = null,
    @Json(name = "parts") val parts: List<MoshiPart>
)

@JsonClass(generateAdapter = true)
data class MoshiPart(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: MoshiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class MoshiInlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<MoshiCandidate>?
)

@JsonClass(generateAdapter = true)
data class MoshiCandidate(
    @Json(name = "content") val content: MoshiContent?
)

// --- Retrofit Network configuration ---

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}
