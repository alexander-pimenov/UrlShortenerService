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

    private val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)

    fun createShortUrl(originalUrl: String, ttlHours: Long? = null): ShortUrl {
        val shortCode = generateShortCode()
        val expiresAt = ttlHours?.let { Instant.now().plus(it, ChronoUnit.HOURS) }

        val shortUrl = ShortUrl(
            shortCode = shortCode,
            originalUrl = originalUrl,
            expiresAt = expiresAt
        )
        // Сохраняем в БД
        val savedUrl = shortUrlRepository.save(shortUrl)
        // Кэшируем в Redis на 1 час
        // Логируем сохранение в Redis
        logger.info("💾 Saving to Redis - key: url:$shortCode, value: $originalUrl, TTL: 1 hour")
        redisTemplate.opsForValue().set(
            "url:$shortCode",
            originalUrl,
            1, TimeUnit.HOURS
        )
        logger.info("✅ Successfully saved to Redis")
        return savedUrl
    }

    fun redirect(shortCode: String): String {
        // Проверяем кэш Redis
        logger.info("🔍 Looking up short code: $shortCode")
        val cachedUrl = redisTemplate.opsForValue().get("url:$shortCode")
        if (cachedUrl != null) {
            logger.info("🎯 Redis HIT - Found in cache: $cachedUrl")
            sendClickEvent(shortCode) //Отправляем событие клика в Kafka ассинхронно
            return cachedUrl
        }
        // Если нет в кэше - ищем в БД
        logger.info("❌ Redis MISS - Not found in cache, querying database")
        val shortUrl = shortUrlRepository.findByShortCode(shortCode)
            ?: throw RuntimeException("Short URL not found in DB")
        // Обновляем кэш
        logger.info("💾 Updating Redis cache - key: url:$shortCode, value: ${shortUrl.originalUrl}")
        redisTemplate.opsForValue().set(
            "url:$shortCode",
            shortUrl.originalUrl,
            1, TimeUnit.HOURS)

        logger.info("✅ Redis cache updated")
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