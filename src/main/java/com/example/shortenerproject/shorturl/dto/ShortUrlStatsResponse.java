package com.example.shortenerproject.shorturl.dto;

public record ShortUrlStatsResponse(
        String shortUrl,
        long countOfTransition
) {
}
