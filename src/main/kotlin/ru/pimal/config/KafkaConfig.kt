package ru.pimal.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import ru.pimal.model.ClickEvent

@Configuration
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")

    // ==================== PRODUCER CONFIG ====================

    // Специфичный бин для ClickEvent
    @Bean
    fun clickEventProducerFactory(): ProducerFactory<String, ClickEvent> {
        val props = HashMap<String, Any>()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        props[ProducerConfig.ACKS_CONFIG] = "all" // Ждем подтверждения от всех реплик
        props[ProducerConfig.RETRIES_CONFIG] = 3
        props[ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION] = 1
        return DefaultKafkaProducerFactory(props)
    }

    @Bean("clickEventKafkaTemplate")
    fun clickEventKafkaTemplate(): KafkaTemplate<String, ClickEvent> {
        return KafkaTemplate(clickEventProducerFactory())
    }

    // ConsumerFactory для ClickEvent
    @Bean
    fun clickEventConsumerFactory(): ConsumerFactory<String, String> {
        val props = HashMap<String, Any>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = "url-shortener-consumer-group"
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
//        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        props[JsonDeserializer.TRUSTED_PACKAGES] = "ru.pimal.model" // Укажите ваш package
        props[JsonDeserializer.VALUE_DEFAULT_TYPE] = "ru.pimal.model.ClickEvent"
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false // Ручное управление коммитами

        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = clickEventConsumerFactory()
        factory.setConcurrency(3) // Количество потоков для обработки

        // Настройка для ручного подтверждения (если нужно)
        // factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL

        return factory
    }

    // Можно добавить другие специфичные шаблоны при необходимости
//    @Bean("stringKafkaTemplate")
//    fun stringKafkaTemplate(): KafkaTemplate<String, String> {
//        val props = HashMap<String, Any>()
//        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
//        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
//        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
//        props[ProducerConfig.ACKS_CONFIG] = "all"
//        val factory = DefaultKafkaProducerFactory<String, String>(props)
//        return KafkaTemplate(factory)
//    }
}


//    @Bean
//    @Primary
//    fun kafkaProperties(): KafkaProperties {
//        return KafkaProperties().apply {
//            bootstrapServers = listOf("localhost:29092")
//            println("--- kafkaProperties (START) ---")
//            println(this.properties.keys)
//            println("--- kafkaProperties (END) ---")
//        }
//    }

//    @Bean
//    fun producerFactory(): ProducerFactory<String, Any> {
//        val props = mutableMapOf<String, Any>()
//        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:29092"
//        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
//        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
//        props[ProducerConfig.ACKS_CONFIG] = "all"
//
//        return DefaultKafkaProducerFactory(props)
//    }
//
//    @Bean
//    fun kafkaTemplate(): KafkaTemplate<String, Any> {
//        return KafkaTemplate(producerFactory())
//    }

//    @Bean
//    fun concurrentKafkaListenerContainerFactory(
//        consumerFactory: ConsumerFactory<String, Any>,
//    ): ConcurrentKafkaListenerContainerFactory<String, Any> {
//        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
//        factory.consumerFactory = consumerFactory
//        factory.recordFilterStrategy { record ->
//            record.value() == null
//        }
//        return factory
//    }

// Основной бин для любых типов
//    @Bean
//    @Primary
//    fun kafkaTemplate(): KafkaTemplate<String, Any> {
//        val props = HashMap<String, Any>()
//        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:29092"
//        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
//        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
//        props[ProducerConfig.ACKS_CONFIG] = "all"
//
//        val factory = DefaultKafkaProducerFactory<String, Any>(props)
//        return KafkaTemplate(factory)
//    }
//
//    // Специфичный бин для ClickEvent
//    @Bean
//    fun clickEventKafkaTemplate(): KafkaTemplate<String, ClickEvent> {
//        return kafkaTemplate() // Используем основной бин
//    }

//    @Bean // Общие настройки producer factory
//    fun clickEventProducerFactory(): ProducerFactory<String, ClickEvent> {
//        val props = HashMap<String, Any>()
//        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] =  bootstrapServers
//        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
//        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
//        props[ProducerConfig.ACKS_CONFIG] = "all"
//        return DefaultKafkaProducerFactory(props)
//    }
//    @Bean("clickEventKafkaTemplate")
//    fun clickEventKafkaTemplate(): KafkaTemplate<String, ClickEvent> {
//        val props = HashMap<String, Any>()
//        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] =  bootstrapServers
//        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
//        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
//        props[ProducerConfig.ACKS_CONFIG] = "all"
//        val factory = DefaultKafkaProducerFactory<String, ClickEvent>(props)
//        return KafkaTemplate(factory)
//    }

//    @Bean
//    fun producerFactory(): ProducerFactory<String, String> {
//        val configDetails = mutableMapOf<String, Any>().apply {
//            putAll(kafkaProducerProperties.toProducerConfig())
//            //доп. настройки, которых нет в yaml
//            //put(...)
//        }
//        return DefaultKafkaProducerFactory(configDetails)
//    }
//
//    @Bean
//    fun kafkaTemplate(): KafkaTemplate<String, String> {
//        return KafkaTemplate(producerFactory())
//    }