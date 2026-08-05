package com.example.data.repository

import com.example.data.local.TranslationHistoryDao
import com.example.data.local.TranslationHistoryEntity
import com.example.data.remote.BackendClient
import com.example.data.remote.BackendTranslationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TranslationRepository(
    private val historyDao: TranslationHistoryDao
) {

    suspend fun translateToCasualEnglish(
        originalText: String,
        sourceLanguage: String = "Auto-detect",
        tone: String = "Casual & Natural"
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmed = originalText.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.success("")
        }

        val request = BackendTranslationRequest(
            text = trimmed,
            sourceLanguage = sourceLanguage,
            tone = tone,
            targetLanguage = "English"
        )

        try {
            val response = BackendClient.service.translate(request)
            val outputText = response.getOutputText()

            if (outputText.isNotBlank()) {
                saveHistory(trimmed, outputText, sourceLanguage, tone)
                Result.success(outputText)
            } else if (!response.error.isNullOrBlank()) {
                Result.failure(Exception(response.error))
            } else {
                Result.success(trimmed)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun saveHistory(
        original: String,
        translated: String,
        sourceLang: String,
        tone: String
    ) {
        try {
            historyDao.insertHistory(
                TranslationHistoryEntity(
                    originalText = original,
                    translatedText = translated,
                    sourceLanguage = sourceLang,
                    tone = tone
                )
            )
        } catch (_: Exception) {
        }
    }
}
