package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.TranslationHistoryEntity
import com.example.data.remote.BackendApi
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiDirectApi
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import com.example.data.remote.TranslationRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

sealed class TranslationResult {
    data class Success(val translatedText: String) : TranslationResult()
    data class Error(val message: String) : TranslationResult()
}

class TranslationRepository(context: Context) {

    companion object {
        const val PRODUCTION_BACKEND_URL = "https://ai-casual-english-backend.onrender.com/api/translate"
        private const val TAG = "TranslationRepo"
    }

    private val historyDao = AppDatabase.getDatabase(context).translationHistoryDao()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val backendRetrofit = Retrofit.Builder()
        .baseUrl("https://ai-casual-english-backend.onrender.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val backendApi = backendRetrofit.create(BackendApi::class.java)

    private val geminiRetrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val geminiApi = geminiRetrofit.create(GeminiDirectApi::class.java)

    val history: Flow<List<TranslationHistoryEntity>> = historyDao.getAllHistory()

    fun searchHistory(query: String): Flow<List<TranslationHistoryEntity>> {
        return historyDao.searchHistory(query)
    }

    suspend fun translateText(
        text: String,
        backendUrl: String = PRODUCTION_BACKEND_URL,
        customGeminiKey: String = ""
    ): TranslationResult = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext TranslationResult.Error("Input text is empty")
        }

        val targetUrl = if (backendUrl.isNotBlank()) {
            if (backendUrl.endsWith("/api/translate")) backendUrl
            else if (backendUrl.endsWith("/")) "${backendUrl}api/translate"
            else "$backendUrl/api/translate"
        } else {
            PRODUCTION_BACKEND_URL
        }

        // 1. Call Production Backend Server with Retry Mechanism (handles Render cold start / sleeping)
        var maxRetries = 3
        var currentAttempt = 0
        var lastErrorMessage = ""

        while (currentAttempt < maxRetries) {
            currentAttempt++
            try {
                Log.d(TAG, "[Attempt $currentAttempt/$maxRetries] POST Request to: $targetUrl | Body: text='$trimmed'")
                
                val response = backendApi.translateText(
                    url = targetUrl,
                    request = TranslationRequest(text = trimmed)
                )

                Log.d(TAG, "Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    val resultText = body?.translated?.trim()
                    Log.d(TAG, "Response Body: translated='$resultText'")

                    if (!resultText.isNullOrEmpty()) {
                        saveToHistory(trimmed, resultText)
                        return@withContext TranslationResult.Success(resultText)
                    } else {
                        lastErrorMessage = "Empty translation returned from server."
                    }
                } else {
                    val errorCode = response.code()
                    lastErrorMessage = "HTTP error $errorCode from server."
                    Log.w(TAG, "Backend error code: $errorCode")

                    // If Render server is sleeping (502, 503, 504), wait for cold start and retry
                    if (errorCode in listOf(502, 503, 504, 429) && currentAttempt < maxRetries) {
                        Log.i(TAG, "Render server may be spinning up. Waiting 2.5s before retry $currentAttempt...")
                        delay(2500)
                        continue
                    }
                }
            } catch (e: Exception) {
                lastErrorMessage = e.localizedMessage ?: "Network error"
                Log.e(TAG, "Network exception on attempt $currentAttempt: ${e.message}")

                if (currentAttempt < maxRetries) {
                    Log.i(TAG, "Retrying request in 2s...")
                    delay(2000)
                }
            }
        }

        // 2. Direct Gemini API Fallback (if API key is available)
        val geminiKeyToUse = customGeminiKey.ifBlank { BuildConfig.GEMINI_API_KEY }
        if (geminiKeyToUse.isNotBlank() && geminiKeyToUse != "MY_GEMINI_API_KEY") {
            try {
                Log.d(TAG, "Attempting direct Gemini API fallback")
                val systemPrompt = """
                    You are a professional multilingual translator and native English writer.
                    Your task is NOT literal translation.
                    First infer the user's intended meaning from input (Hindi, Roman Hindi, Odia, Roman Odia, Bengali, Tamil, Telugu, Kannada, Malayalam, Gujarati, Punjabi, Urdu, or mixed language).
                    Then rewrite it into natural, fluent, everyday spoken English.
                    The final sentence must sound exactly like something a native speaker would type in WhatsApp.
                    Never sound robotic. Never translate word-for-word. Never explain. Return ONLY the rewritten English sentence.
                """.trimIndent()

                val geminiReq = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = trimmed)))),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
                )

                val geminiResp = geminiApi.generateContent(geminiKeyToUse, geminiReq)
                if (geminiResp.isSuccessful) {
                    val candidate = geminiResp.body()?.candidates?.firstOrNull()
                    val resultText = candidate?.content?.parts?.firstOrNull()?.text?.trim()
                    if (!resultText.isNullOrEmpty()) {
                        saveToHistory(trimmed, resultText)
                        return@withContext TranslationResult.Success(resultText)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini fallback exception: ${e.message}")
            }
        }

        return@withContext TranslationResult.Error(
            if (lastErrorMessage.isNotBlank()) lastErrorMessage else "Failed to connect to translation server."
        )
    }

    private suspend fun saveToHistory(original: String, translated: String) {
        try {
            historyDao.insertHistory(
                TranslationHistoryEntity(
                    originalText = original,
                    translatedText = translated,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save history: ${e.message}")
        }
    }

    suspend fun deleteHistoryItem(id: Long) = historyDao.deleteById(id)

    suspend fun clearHistory() = historyDao.clearAll()
}

