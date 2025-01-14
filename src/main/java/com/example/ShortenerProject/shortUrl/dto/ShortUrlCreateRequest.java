package com.example.ShortenerProject.shortUrl.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortUrlCreateRequest {
    private String shortUrl;
    private String originUrl;
    private String dateOfCreating;
    private String dateOfExpiring;
}