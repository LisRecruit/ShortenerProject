package com.example.shortenerproject.shorturl.dto.response;

public record ShortUrlStatsResponse(
        String shortUrl,
        long countOfTransition
) {
}
