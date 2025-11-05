package ru.pimal.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import ru.pimal.dao.ShortUrlRepository
import ru.pimal.model.ClickEvent

/**
 * Обработчик Kafka для статистики
 *
 * 🔍 Почему нужен @Transactional:
 * - Методы, помеченные @Modifying и @Query с UPDATE/DELETE, требуют активной транзакции
 * - Обработчики Kafka @KafkaListener по умолчанию не запускаются в транзакционном контексте
 * - @Transactional создает транзакцию для метода
 *
 *
 */
@Service
@Transactional // ← Добавляем аннотацию на уровне класса, но можно и на уровне метода
class ClickStatisticsService(
    private val shortUrlRepository: ShortUrlRepository,
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
) {

    private val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        topics = ["url-clicks"],
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun processClick(record: ConsumerRecord<String, String>) {
        try {
            logger.info("\uD83D\uDCCA Received click event: ${record.value()}")
            // Обновляем статистику кликов, Асинхронно обновляем счетчик в БД
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            val event = objectMapper.readValue(record.value(), ClickEvent::class.java)
            logger.info("✅ Click event: $event")
            shortUrlRepository.incrementClickCount(event.shortCode)
            logger.info("✅ Click count updated for: ${event.shortCode}")
        } catch (e: Exception) {
            logger.error("❌ Error processing click event: exception=${e.javaClass.simpleName}.  ${e.message}", e)
            // Здесь можно добавить логику повторной обработки или dead letter queue
        }
    }
}