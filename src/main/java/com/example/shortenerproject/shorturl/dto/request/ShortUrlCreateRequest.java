package com.example.shortenerproject.shorturl.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;


@Schema(description = "Request to create a shortened URL")
public record ShortUrlCreateRequest (
    @NotEmpty(message = "Origin URL cannot be empty")
    @Schema(description = "Original URL", example = "https://example.com")
    String originUrl,

    @NotNull(message = "User ID cannot be null")
    @Schema(description = "User ID", example = "1")
    Long user
){}