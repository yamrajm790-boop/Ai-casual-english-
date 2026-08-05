package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TranslationRequest(
    @Json(name = "text") val text: String,
    @Json(name = "style") val style: String? = "casual"
)
