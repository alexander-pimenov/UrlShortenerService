package ru.pimal.util

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import ru.pimal.model.ClickEvent
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

/**
 * ApplicationRunner интерфейс гарантирует выполнение кода после запуска приложения
 */
@Component
class ConnectionTest(
    private val dataSource: DataSource,
    private val redisTemplate: RedisTemplate<String, String>,
//    @Qualifier("stringKafkaTemplate")
//    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Qualifier("clickEventKafkaTemplate")
    private val kafkaTemplate: KafkaTemplate<String, ClickEvent>
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        println("Testing connections...")
        //Test Postgres
        testPostgreSQL()
        //Try Redis
        testRedis()
        //Try Kafka
        testKafka()
        println("🎯 All connection tests completed!")
    }

    private fun testKafka() {
        try {
            //тест прод.сера - отправка сообщения
    //            val record = ProducerRecord("connection-test-topic", "test-key", "test-value")
            val testEvent = ClickEvent("test-connection", Instant.now())
            println("\uD83D\uDE80 Sending message to Kafka...")
            val future = kafkaTemplate.send("test-topic", "test-key", testEvent)
    //            val future = kafkaTemplate.send(record)
            // Ждем отправки (можно сделать асинхронно, но для теста синхронно)
            val result = future.get(5, TimeUnit.SECONDS)
            println("✅ Kafka connected successfully - message sent to partition ${result.recordMetadata.partition()}")
            // Подождем немного, чтобы убедиться, что консюмер тоже работает
            Thread.sleep(2000)
        } catch (e: Exception) {
            println("❌ Kafka connection failed: ${e.message}")
        }
    }

    private fun testRedis() {
        try {
            // Базовый тест
            redisTemplate.opsForValue().set("connection-test", "success")
            val result = redisTemplate.opsForValue().get("connection-test")
            if (result != null) {
                println("✅ Redis connected successfully")
                // Дополнительная информация
                val info = redisTemplate.execute { connection ->
                    connection.serverCommands().info("memory")
                }
                println("📊 Redis memory info: $info")

                // Посчитать количество закэшированных URL
                val urlKeys = redisTemplate.keys("url:*")
                println("📈 Cached URLs in Redis: ${urlKeys.size}")
            } else {
                println("❌ Redis test failed")
            }
            redisTemplate.delete("connection-test")
        } catch (e: Exception) {
            println("❌ Redis connection failed: ${e.message}")
        }
    }

    private fun testPostgreSQL() {
        try {
            dataSource.connection.use { connection ->
                val valid = connection.isValid(5000) //5 seconds
                if (valid) {
                    println("✅ PostgreSQL connection successful")
                    //Test basic query
                    connection.createStatement().use { statement ->
                        statement.execute("SELECT 1")
                        println("✅ PostgreSQL query executed successfully")
                    }
                } else {
                    println("❌ PostgreSQL connection is invalid")
                }
            }
        } catch (e: Exception) {
            println("Postgres connection failed: ${e.message}")
        }
    }
}