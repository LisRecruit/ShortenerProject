package com.example.ShortenerProject.shortUrl.dto;

import lombok.Builder;

@Builder
public record ShortUrlResponse(
        String shortUrl,
        String originUrl,
        String dateOfCreating,
        String dateOfExpiring,
        Long user
) {}