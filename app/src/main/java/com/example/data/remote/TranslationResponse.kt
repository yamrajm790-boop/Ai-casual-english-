package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TranslationResponse(
    @Json(name = "translated") val translated: String? = null,
    @Json(name = "error") val error: String? = null
)
