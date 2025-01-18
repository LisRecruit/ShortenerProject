package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.shortUrl.dto.ShortUrlCreateRequest;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlResponse;

import com.example.ShortenerProject.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShortUrlMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "countOfTransition", constant = "0l")
    @Mapping(target = "user", ignore = true)
    ShortUrl toEntity(ShortUrlCreateRequest request);

    @Mapping(target = "user", source = "user.id") // Використовуємо ідентифікатор User
    ShortUrlResponse toResponse(ShortUrl entity);

    default Long map(User user) {
        return user != null ? user.getId() : null;
    }
}
