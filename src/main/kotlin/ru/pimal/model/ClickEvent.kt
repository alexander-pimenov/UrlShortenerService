package ru.pimal.model

import java.time.Instant

/**
 * ClickEvent Kafka для статистики
 */
data class ClickEvent(
    val shortCode: String,
    val timestamp: Instant
)
