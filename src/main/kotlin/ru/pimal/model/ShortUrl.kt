package ru.pimal.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "short_urls")
data class ShortUrl(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, unique = true)
    val shortCode: String,
    @Column(nullable = false, length = 2048)
    val originalUrl: String,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column
    val expiresAt: Instant? = null,
    @Column
    var clickCount: Long = 0,

    )