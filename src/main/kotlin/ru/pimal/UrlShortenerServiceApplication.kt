package ru.pimal

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UrlShortenerServiceApplication

fun main(args: Array<String>) {
	runApplication<UrlShortenerServiceApplication>(*args)
}
