package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.shortUrl.dto.ShortUrlCreateRequest;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlResponse;
import com.example.ShortenerProject.user.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class ShortUrlMapperTest {
    private final ShortUrlMapper shortUrlMapper = Mappers.getMapper(ShortUrlMapper.class);

    @Test
    void shouldMapShortUrlCreateRequestToEntity() {
        // Arrange
        ShortUrlCreateRequest request = new ShortUrlCreateRequest();
        request.setOriginUrl("https://example.com");
        request.setUser(1L); // Устанавливаем ID пользователя

        ShortUrl shortUrl = shortUrlMapper.toEntity(request);

        assertEquals(0L, shortUrl.getCountOfTransition());
        assertNull(shortUrl.getUser());

    }


    @Test
    void shouldMapShortUrlToShortUrlResponse() {
        User user = new User();
        user.setId(1L);
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setId(1L);
        shortUrl.setShortUrl("http://short.url");
        shortUrl.setUser(user);

        ShortUrlResponse response = shortUrlMapper.toResponse(shortUrl);

        assertEquals(1L, response.user());
        assertEquals(1L, response.user());
        assertEquals("http://short.url", response.shortUrl());
    }

    @Test
    void shouldMapUserToId() {
        User user = new User();
        user.setId(1L);

        Long userId = shortUrlMapper.map(user);

        assertEquals(1L, userId);
    }

    @Test
    void shouldReturnNullForNullUser() {
        User user = null;

        Long userId = shortUrlMapper.map(user);

        assertNull(userId);
    }

}