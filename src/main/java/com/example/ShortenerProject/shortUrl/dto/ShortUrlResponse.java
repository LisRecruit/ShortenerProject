package com.example.ShortenerProject.shortUrl.dto;

import com.example.ShortenerProject.shortUrl.ShortUrl;
import lombok.Builder;

@Builder
public record ShortUrlResponse(
        String shortUrl,
        String originUrl,
        String dateOfCreating,
        String dateOfExpiring,
        String userName
) {

    // Щоб змапити ShortUrl entity до ShortUrlResponse
    public static ShortUrlResponse fromShortUrl(ShortUrl shortUrl) {
        return ShortUrlResponse.builder()
                .shortUrl(shortUrl.getShortUrl())
                .originUrl(shortUrl.getOriginUrl())
                .dateOfCreating(shortUrl.getDateOfCreating())
                .dateOfExpiring(shortUrl.getDateOfExpiring())
                .userName(shortUrl.getUser().getUsername())
                .build();
    }
}
