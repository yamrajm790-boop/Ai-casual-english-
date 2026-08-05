package com.example.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.IOException
import java.util.concurrent.TimeUnit

@Serializable
data class BackendTranslationRequest(
    val text: String,
    val sourceLanguage: String? = "Auto-detect",
    val tone: String? = "Casual",
    val targetLanguage: String? = "English"
)

@Serializable
data class BackendTranslationResponse(
    val translated: String? = null,
    val translatedText: String? = null,
    val result: String? = null,
    val error: String? = null
) {
    fun getOutputText(): String {
        return (translated ?: translatedText ?: result ?: "").trim()
    }
}

interface BackendApiService {
    @POST("api/translate")
    suspend fun translate(
        @Body request: BackendTranslationRequest
    ): BackendTranslationResponse
}

class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var tryCount = 0

        while (tryCount < maxRetries) {
            try {
                response = chain.proceed(request)
                if (response.isSuccessful) {
                    return response
                }
                response.close()
            } catch (e: IOException) {
                exception = e
            }
            tryCount++
            try {
                Thread.sleep(1000L * tryCount) // Exponential backoff for Render cold start
            } catch (_: InterruptedException) {}
        }

        if (response != null) {
            return response
        }
        throw exception ?: IOException("Failed to communicate with translation backend after $maxRetries attempts")
    }
}

object BackendClient {
    private const val BASE_URL = "https://ai-casual-english-backend.onrender.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .addInterceptor(RetryInterceptor(maxRetries = 3))
        .build()

    val service: BackendApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(BackendApiService::class.java)
    }
}
