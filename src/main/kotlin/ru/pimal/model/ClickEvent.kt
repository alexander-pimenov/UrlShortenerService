package ru.pimal.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant

/**
 * ClickEvent Kafka для статистики
 */
data class ClickEvent(
    val shortCode: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'", timezone = "UTC")
    val timestamp: Instant = Instant.now()
)
