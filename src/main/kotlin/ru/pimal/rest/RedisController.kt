package ru.pimal.rest

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер для мониторинга Redis
 */
@RestController
@RequestMapping("/api/redis")
class RedisController(
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)

    @GetMapping("/stats")
    fun getRedisStats(): Map<String, Any> {
        val keys = redisTemplate.keys("url:*")
        val stats = mutableMapOf<String, Any>()

        stats["totalCachedUrls"] = keys.size
        stats["cachedKeys"] = keys.take(10) // первые 10 ключей

        // TTL для первых 5 ключей
        val ttlInfo = keys.take(5).associate { key ->
            val ttl = redisTemplate.getExpire(key)
            key to "${ttl} seconds"
        }
        stats["sampleTTLs"] = ttlInfo

        return stats
    }

    @GetMapping("/keys/{pattern}")
    fun getKeys(@PathVariable pattern: String): List<String> {
        return redisTemplate.keys("$pattern*").toList()
    }

    @GetMapping("/value/{key}")
    fun getValue(@PathVariable key: String): String? {
        return redisTemplate.opsForValue().get(key)
    }
}