package ru.pimal.rest

data class ShortenUrlRequest(
    val url: String,
    val ttlHours: Long? = null
)

data class ShortUrlResponse(
    val shortCode: String,
    val originalUrl: String,
    val shortUrl: String
)