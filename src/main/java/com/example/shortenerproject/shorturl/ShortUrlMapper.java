package com.example.shortenerproject.shorturl;

import com.example.shortenerproject.shorturl.dto.request.ShortUrlCreateRequest;
import com.example.shortenerproject.shorturl.dto.response.ShortUrlResponse;

import com.example.shortenerproject.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShortUrlMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "countOfTransition", constant = "0L")
    @Mapping(target = "user", ignore = true)
    ShortUrl toEntity(ShortUrlCreateRequest request);

    @Mapping(target = "user", source = "user.id") // Використовуємо ідентифікатор User
    ShortUrlResponse toResponse(ShortUrl entity);

    default Long map(User user) {
        return user != null ? user.getId() : null;
    }
}
