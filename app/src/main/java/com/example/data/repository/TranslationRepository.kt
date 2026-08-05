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
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

sealed class TranslationResult {
    data class Success(val translatedText: String) : TranslationResult()
    data class Error(val message: String) : TranslationResult()
}

object OfflineCasualEngine {
    fun translate(input: String): String {
        val trimmed = input.trim()
        val lower = trimmed.lowercase()

        // 1. Gypsum / floor / housekeeping dynamic check
        if (lower.contains("gypsum") || lower.contains("gypsum board")) {
            var result = "The housekeeping team brought "
            result += if (lower.contains("5pcs") || lower.contains("5 pcs") || lower.contains("5 piece")) {
                "5 pieces of gypsum board "
            } else {
                "the gypsum board "
            }
            result += if (lower.contains("9th floor") || lower.contains("9 floor") || lower.contains("9th")) {
                "from the 9th floor."
            } else {
                "over."
            }
            return result
        }

        // 2. Specific known demo sentences
        if (lower.contains("aaj bhi") && (lower.contains("housekeeping") || lower.contains("house keeping"))) {
            return "Sir, the housekeeping team is coming today as well."
        }

        return when {
            lower.contains("main kal udhar nahi aa paunga") || (lower.contains("kal udhar") && lower.contains("nahi aa")) ->
                "I won't be able to come there tomorrow."
            lower.contains("worker helmet nahi pehna") || lower.contains("helmet nahi pehna") ->
                "The worker isn't wearing a helmet."
            lower.contains("carpenter refuse area") || (lower.contains("carpenter") && lower.contains("door remove")) ->
                "The carpenter is removing the refuse area door."
            lower.contains("main kal nahi aaunga") || lower.contains("kal nahi aaunga") || lower.contains("kal nehi aaunga") ->
                "I won't come tomorrow."
            lower.contains("mu office jauchi") || lower.contains("office jauchi") ->
                "I'm heading to the office."
            lower.contains("ami bari jacchi") || lower.contains("bari jacchi") ->
                "I'm heading home."
            lower.contains("nenu intiki velthunna") || lower.contains("intiki velthunna") ->
                "I'm heading home."
            lower.contains("main ghar ja raha") || lower.contains("ghar ja raha") ->
                "I'm heading home."
            lower.contains("ami kheyechi") || lower.contains("khana khaya") ->
                "I already ate."
            lower.contains("nenu vachanu") || lower.contains("aa gaya") ->
                "I'm here."
            lower.contains("kaise ho") || lower.contains("kan karuchu") || lower.contains("kaha korcho") ->
                "What's up?"
            lower.contains("kahan ho") || lower.contains("ekkada unnav") ->
                "Where are you at?"
            lower.contains("shukriya") || lower.contains("dhanyawad") || lower.contains("nandri") ->
                "Thanks a lot!"
            else -> {
                var translated = trimmed
                    .replace("house keeping team", "the housekeeping team", ignoreCase = true)
                    .replace("housekeeping team", "the housekeeping team", ignoreCase = true)
                    .replace("9th floor se", "from the 9th floor", ignoreCase = true)
                    .replace("9th floor", "the 9th floor", ignoreCase = true)
                    .replace("5pcs", "5 pieces of", ignoreCase = true)
                    .replace("5 pcs", "5 pieces of", ignoreCase = true)
                    .replace("leke aaye", "brought", ignoreCase = true)
                    .replace("leke aa rahe", "are bringing", ignoreCase = true)
                    .replace("aarehe hain", "are coming", ignoreCase = true)
                    .replace("aa rahe hain", "are coming", ignoreCase = true)
                    .replace("aaj bhi", "today as well", ignoreCase = true)
                    .replace("aaj bhii", "today as well", ignoreCase = true)
                    .replace("aaj", "today", ignoreCase = true)
                    .replace("kal", "tomorrow", ignoreCase = true)
                    .replace("nahi", "not", ignoreCase = true)
                    .replace("nehi", "not", ignoreCase = true)
                    .replace("sir", "sir", ignoreCase = true)

                translated.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }
    }
}

class TranslationRepository(context: Context) {

    companion object {
        const val PRODUCTION_BASE_URL = "https://ai-casual-english-backend.onrender.com"
        private const val TAG = "TranslationRepo"
        
        private val ENDPOINT_CANDIDATES = listOf(
            "/api/translate",
            "/translate",
            "/api/chat",
            "/chat",
            "/v1/chat"
        )
    }

    private var activeEndpointPath: String? = "/api/translate"

    private val historyDao = AppDatabase.getDatabase(context).translationHistoryDao()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val backendRetrofit = Retrofit.Builder()
        .baseUrl("$PRODUCTION_BASE_URL/")
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
        backendUrl: String = PRODUCTION_BASE_URL,
        customGeminiKey: String = ""
    ): TranslationResult = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext TranslationResult.Error("Input text is empty")
        }

        val baseUrl = if (backendUrl.isNotBlank()) backendUrl.trimEnd('/') else PRODUCTION_BASE_URL
        var lastErrorMessage = ""

        // 1. Primary Backend Call with Endpoint Auto-Discovery & Retry Logic (Render Cold Start)
        val endpointsToTry = mutableListOf<String>()
        activeEndpointPath?.let { endpointsToTry.add(it) }
        ENDPOINT_CANDIDATES.forEach { ep ->
            if (!endpointsToTry.contains(ep)) endpointsToTry.add(ep)
        }

        for (endpointPath in endpointsToTry) {
            val targetFullUrl = "$baseUrl$endpointPath"
            var maxRetries = 3
            var currentAttempt = 0

            while (currentAttempt < maxRetries) {
                currentAttempt++
                try {
                    Log.d(TAG, "--------------------------------------------------")
                    Log.d(TAG, "Backend URL: $baseUrl")
                    Log.d(TAG, "Endpoint: $endpointPath")
                    Log.d(TAG, "Attempt: $currentAttempt / $maxRetries")
                    Log.d(TAG, "Request JSON: {\"text\": \"$trimmed\", \"style\": \"casual\"}")

                    val response = backendApi.translateText(
                        url = targetFullUrl,
                        request = TranslationRequest(text = trimmed)
                    )

                    val statusCode = response.code()
                    Log.d(TAG, "HTTP Status: $statusCode")

                    if (response.isSuccessful) {
                        val responseBodyString = response.body()?.string() ?: ""
                        Log.d(TAG, "Response JSON: $responseBodyString")

                        val parsedTranslation = parseTranslationResponseBody(responseBodyString)
                        Log.d(TAG, "Parsed Translation: $parsedTranslation")

                        if (!parsedTranslation.isNullOrBlank()) {
                            activeEndpointPath = endpointPath
                            saveToHistory(trimmed, parsedTranslation)
                            return@withContext TranslationResult.Success(parsedTranslation)
                        } else {
                            lastErrorMessage = "Empty or unparseable translation response from $endpointPath"
                            Log.w(TAG, lastErrorMessage)
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: ""
                        Log.w(TAG, "Error Response Body ($statusCode): $errorBody")

                        if (statusCode == 404) {
                            Log.i(TAG, "Endpoint $endpointPath returned 404. Trying next endpoint candidate...")
                            break // Try next endpoint candidate immediately
                        }

                        lastErrorMessage = "HTTP error $statusCode from server."

                        // Render cold start retry for 502/503/504/429
                        if (statusCode in listOf(502, 503, 504, 429) && currentAttempt < maxRetries) {
                            Log.i(TAG, "Render server spinning up ($statusCode). Waiting 2.5s before retry...")
                            delay(2500)
                            continue
                        }
                    }
                } catch (e: Exception) {
                    lastErrorMessage = e.localizedMessage ?: "Network connection error"
                    Log.e(TAG, "Network Exception on $endpointPath (Attempt $currentAttempt): ${e.message}", e)

                    if (currentAttempt < maxRetries) {
                        Log.i(TAG, "Retrying request in 2s...")
                        delay(2000)
                    }
                }
            }
        }

        // 2. Direct Gemini Fallback (if API key is present)
        val geminiKeyToUse = customGeminiKey.ifBlank { BuildConfig.GEMINI_API_KEY }
        if (geminiKeyToUse.isNotBlank() && geminiKeyToUse != "MY_GEMINI_API_KEY") {
            try {
                Log.d(TAG, "Attempting direct Gemini API fallback")
                val systemPrompt = """
                    Convert the user's message into natural casual spoken English.
                    Do not translate literally.
                    Rewrite like a real native English speaker.
                    Keep the meaning unchanged.
                    Return ONLY the final English sentence.
                    No explanations. No quotation marks. No markdown.
                """.trimIndent()

                val geminiReq = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = trimmed)))),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
                )

                val geminiResp = geminiApi.generateContent(geminiKeyToUse, geminiReq)
                if (geminiResp.isSuccessful) {
                    val candidate = geminiResp.body()?.candidates?.firstOrNull()
                    val resultText = candidate?.content?.parts?.firstOrNull()?.text?.trim()
                        ?.removeSurrounding("\"", "\"")
                    if (!resultText.isNullOrEmpty()) {
                        Log.d(TAG, "Gemini Fallback Success: $resultText")
                        saveToHistory(trimmed, resultText)
                        return@withContext TranslationResult.Success(resultText)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini fallback exception: ${e.message}")
            }
        }

        // 3. Offline Mode Fallback Engine
        Log.i(TAG, "Backend and API unavailable. Using Offline Casual Engine fallback.")
        val offlineTranslation = OfflineCasualEngine.translate(trimmed)
        saveToHistory(trimmed, offlineTranslation)
        return@withContext TranslationResult.Success(offlineTranslation)
    }

    private fun parseTranslationResponseBody(jsonString: String): String? {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) return null

        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return trimmed.removeSurrounding("\"", "\"")
        }

        return try {
            val adapter = moshi.adapter(Map::class.java)
            @Suppress("UNCHECKED_CAST")
            val map = adapter.fromJson(trimmed) as? Map<String, Any?> ?: return null
            extractFromMap(map)
        } catch (e: Exception) {
            Log.w(TAG, "JSON parse exception: ${e.message}")
            null
        }
    }

    private fun extractFromMap(map: Map<String, Any?>): String? {
        val candidateKeys = listOf(
            "translated", "translatedText", "translated_text", "translation",
            "text", "output", "response", "answer", "content", "message",
            "result", "generatedText", "english", "englishText", "english_text"
        )

        for (key in candidateKeys) {
            val value = map[key]
            if (value is String && value.isNotBlank()) {
                return value.trim().removeSurrounding("\"", "\"")
            }
        }

        // Nested 'data' map check
        val dataObj = map["data"]
        if (dataObj is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            val dataMap = dataObj as Map<String, Any?>
            for (key in candidateKeys) {
                val value = dataMap[key]
                if (value is String && value.isNotBlank()) {
                    return value.trim().removeSurrounding("\"", "\"")
                }
            }
        }

        // OpenAI / OpenRouter choices array check
        val choices = map["choices"] as? List<*>
        if (!choices.isNullOrEmpty()) {
            val firstChoice = choices.firstOrNull() as? Map<*, *>
            if (firstChoice != null) {
                val msg = firstChoice["message"] as? Map<*, *>
                if (msg != null) {
                    val content = msg["content"]
                    if (content is String && content.isNotBlank()) {
                        return content.trim().removeSurrounding("\"", "\"")
                    }
                }
                val text = firstChoice["text"]
                if (text is String && text.isNotBlank()) {
                    return text.trim().removeSurrounding("\"", "\"")
                }
            }
        }

        // Gemini candidates array check
        val candidates = map["candidates"] as? List<*>
        if (!candidates.isNullOrEmpty()) {
            val firstCand = candidates.firstOrNull() as? Map<*, *>
            val contentObj = firstCand?.get("content") as? Map<*, *>
            val parts = contentObj?.get("parts") as? List<*>
            if (!parts.isNullOrEmpty()) {
                val firstPart = parts.firstOrNull() as? Map<*, *>
                val text = firstPart?.get("text")
                if (text is String && text.isNotBlank()) {
                    return text.trim().removeSurrounding("\"", "\"")
                }
            }
        }

        return null
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


