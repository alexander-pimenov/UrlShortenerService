package ru.pimal.dao

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import ru.pimal.model.ShortUrl

interface ShortUrlRepository: JpaRepository<ShortUrl, Long> {

    fun findByShortCode(shortCode: String): ShortUrl?

    @Modifying
    @Query("UPDATE ShortUrl s SET s.clickCount = s.clickCount + 1 WHERE s.shortCode = :shortCode")
    fun incrementClickCount(shortCode: String)
}