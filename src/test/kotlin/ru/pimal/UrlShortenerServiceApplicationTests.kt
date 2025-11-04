package ru.pimal

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate

@SpringBootTest
class UrlShortenerServiceApplicationTests {

	@Autowired
	private lateinit var kafkaTemplate: KafkaTemplate<String, String>

	@Test
	fun contextLoads() {
		kafkaTemplate.send("test-test-topic", "Hello, Kafka!")
	}

}
