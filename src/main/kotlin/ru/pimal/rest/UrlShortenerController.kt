package ru.pimal.rest

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.pimal.service.UrlShorteningService
import java.net.URI

@RestController
class UrlShortenerController(
    private val urlShorteningService: UrlShorteningService,
) {
    @PostMapping("/shorten")
    fun shortenUrl(@RequestBody request: ShortenUrlRequest): ShortUrlResponse {
        val shortUrl = urlShorteningService.createShortUrl(
            request.url,
            request.ttlHours
        )
        return ShortUrlResponse(
            shortCode = shortUrl.shortCode,
            originalUrl = shortUrl.originalUrl,
            shortUrl = "http://localhost:8080/${shortUrl.shortCode}"
        )
    }

    @GetMapping("/{shortCode}")
    fun redirect(@PathVariable shortCode: String): ResponseEntity<Void> {
        val originalUrl = urlShorteningService.redirect(shortCode)
        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(
                URI.create(originalUrl)
            )
//            .header("Location", originalUrl)
            .build()
    }
}