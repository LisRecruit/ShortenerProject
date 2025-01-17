package com.example.ShortenerProject.shortUrl.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortUrlCreateRequest {
    @NotEmpty(message = "Short URL cannot be empty")
    private String shortUrl;

    @NotEmpty(message = "Origin URL cannot be empty")
    private String originUrl;

    private String dateOfCreating;
    private String dateOfExpiring;
}