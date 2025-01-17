package com.example.ShortenerProject.shortUrl.dto;

public record ShortUrlStatsResponse(
        String shortUrl,
        long countOfTransition
) {
}
