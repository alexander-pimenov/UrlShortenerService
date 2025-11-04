package ru.pimal.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import ru.pimal.dao.ShortUrlRepository
import ru.pimal.model.ClickEvent
import ru.pimal.model.ShortUrl
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Service
class UrlShorteningService(
    private val shortUrlRepository: ShortUrlRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    @Qualifier("clickEventKafkaTemplate")
    private val kafkaTemplate: KafkaTemplate<String, ClickEvent>,
) {

    fun createShortUrl(originalUrl: String, ttlHours: Long? = null): ShortUrl {
        val shortCode = generateShortCode()
        val expiresAt = ttlHours?.let { Instant.now().plus(it, ChronoUnit.HOURS) }

        val shortUrl = ShortUrl(
            shortCode = shortCode,
            originalUrl = originalUrl,
            expiresAt = expiresAt
        )
        //Сохраняем в БД
        val savedUrl = shortUrlRepository.save(shortUrl)
        //Кэшируем в Redis на 1 час
        redisTemplate.opsForValue().set(shortCode, originalUrl, 1, TimeUnit.HOURS)

        return savedUrl
    }

    fun redirect(shortCode: String): String {
        // Проверяем кэш Redis
        val cachedUrl = redisTemplate.opsForValue().get(shortCode)
        if (cachedUrl != null) {
            sendClickEvent(shortCode) //Отправляем событие клика в Kafka ассинхронно
            return cachedUrl
        }
        // Если нет в кэше - ищем в БД
        val shortUrl = shortUrlRepository.findByShortCode(shortCode)
            ?: throw RuntimeException("Short URL not found in DB")
        //Обновляем кэш
        redisTemplate.opsForValue().set(shortCode, shortUrl.originalUrl, 1, TimeUnit.HOURS)

        sendClickEvent(shortCode) //Отправляем событие клика в Kafka ассинхронно
        return shortUrl.originalUrl
    }

    private fun sendClickEvent(shortCode: String) {
        val event = ClickEvent(shortCode, Instant.now())
        kafkaTemplate.send("url-clicks", shortCode, event)
    }

    private fun generateShortCode(): String {
        // Генерируем случайный код из 6 символов
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}