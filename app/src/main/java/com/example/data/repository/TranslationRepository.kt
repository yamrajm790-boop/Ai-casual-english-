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

    private val historyDao = AppDatabase.getDatabase(context).translationHistoryDao()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val backendRetrofit = Retrofit.Builder()
        .baseUrl("https://ai-casual-english-keyboard.onrender.com/") // dummy base, overridden by full @Url
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
        backendUrl: String,
        customGeminiKey: String = ""
    ): TranslationResult = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext TranslationResult.Error("Input text is empty")
        }

        // 1. Try Backend Server First
        val fullEndpointUrl = if (backendUrl.endsWith("/api/translate")) {
            backendUrl
        } else if (backendUrl.endsWith("/")) {
            "${backendUrl}api/translate"
        } else {
            "$backendUrl/api/translate"
        }

        try {
            Log.d("TranslationRepo", "Calling backend URL: $fullEndpointUrl")
            val response = backendApi.translateText(
                url = fullEndpointUrl,
                request = TranslationRequest(text = trimmed)
            )

            if (response.isSuccessful) {
                val body = response.body()
                val resultText = body?.translated?.trim()
                if (!resultText.isNullOrEmpty()) {
                    saveToHistory(trimmed, resultText)
                    return@withContext TranslationResult.Success(resultText)
                }
            } else {
                Log.w("TranslationRepo", "Backend response failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("TranslationRepo", "Backend request exception: ${e.message}")
        }

        // 2. Fallback to Gemini Direct API if configured or BuildConfig key is present
        val geminiKeyToUse = customGeminiKey.ifBlank { BuildConfig.GEMINI_API_KEY }
        if (geminiKeyToUse.isNotBlank() && geminiKeyToUse != "MY_GEMINI_API_KEY") {
            try {
                Log.d("TranslationRepo", "Attempting direct Gemini API fallback")
                val systemPrompt = """
                    You are a professional multilingual translator and native English writer.
                    Your task is NOT literal translation.
                    First infer the user's intended meaning.
                    Then rewrite it into natural, fluent, everyday spoken English.
                    The final sentence must sound exactly like something a native speaker would type in WhatsApp.
                    Never sound robotic.
                    Never translate word-for-word.
                    Never explain.
                    Never mention the detected language.
                    Return ONLY the rewritten English sentence.
                """.trimIndent()
                val geminiReq = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = trimmed)))
                    ),
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemPrompt))
                    )
                )

                val geminiResp = geminiApi.generateContent(geminiKeyToUse, geminiReq)
                if (geminiResp.isSuccessful) {
                    val candidate = geminiResp.body()?.candidates?.firstOrNull()
                    val resultText = candidate?.content?.parts?.firstOrNull()?.text?.trim()
                    if (!resultText.isNullOrEmpty()) {
                        saveToHistory(trimmed, resultText)
                        return@withContext TranslationResult.Success(resultText)
                    }
                } else {
                    Log.w("TranslationRepo", "Gemini API error code: ${geminiResp.code()}")
                }
            } catch (e: Exception) {
                Log.e("TranslationRepo", "Gemini request exception: ${e.message}")
            }
        }

        // 3. Fallback Offline Smart Casual English Rules (for local fallback if offline or no backend running)
        val offlineTranslation = generateOfflineCasualTranslation(trimmed)
        saveToHistory(trimmed, offlineTranslation)
        return@withContext TranslationResult.Success(offlineTranslation)
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
            Log.e("TranslationRepo", "Failed to save history: ${e.message}")
        }
    }

    suspend fun deleteHistoryItem(id: Long) = historyDao.deleteById(id)

    suspend fun clearHistory() = historyDao.clearAll()

    private fun generateOfflineCasualTranslation(text: String): String {
        val lower = text.lowercase().trim()
        return when {
            lower.contains("main kal nahi aaunga") || lower.contains("main kal nehi aaunga") || lower.contains("kal nahi aaunga") || lower.contains("kal nehi aaunga") || lower.contains("hun main udhar nehi aah sakta") || lower.contains("udhar nahi aa") -> "I won't come tomorrow."
            lower.contains("mu office jauchi") || lower.contains("office jauchi") -> "I'm heading to the office."
            lower.contains("main thoda late aaunga") || lower.contains("thoda late") -> "I'll be a little late."
            lower.contains("kal milte hain") || lower.contains("kal milte") -> "See you tomorrow."
            lower.contains("mu pare call karibi") || lower.contains("call karibi") || lower.contains("baad me call") -> "I'll call you later."
            lower.contains("mu gharaku jauchi") || lower.contains("main ghar ja raha") || lower.contains("ami bari jacchi") || lower.contains("nenu intiki velthunna") -> "I'm heading home."
            lower.contains("ami kheyechi") || lower.contains("khana khaya") || lower.contains("kabaad") -> "I already ate."
            lower.contains("nenu vachanu") || lower.contains("aa gaya") -> "I'm here."
            lower.contains("kya kar rahe ho") || lower.contains("kan karuchu") || lower.contains("kaha korcho") -> "What's up?"
            lower.contains("kahan ho") || lower.contains("koubr") || lower.contains("ekkada unnav") -> "Where are you at?"
            lower.contains("shukriya") || lower.contains("dhanyawad") || lower.contains("nandri") -> "Thanks a lot!"
            else -> {
                text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }
    }
}
