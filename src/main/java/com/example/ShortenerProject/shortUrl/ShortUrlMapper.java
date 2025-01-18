package com.example.ShortenerProject.shortUrl;

import com.example.ShortenerProject.shortUrl.dto.ShortUrlCreateRequest;
import com.example.ShortenerProject.shortUrl.dto.ShortUrlResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShortUrlMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "countOfTransition", constant = "0")
    @Mapping(target = "user", ignore = true)
    ShortUrl toEntity(ShortUrlCreateRequest request);

    ShortUrlResponse toResponse(ShortUrl entity);
}
